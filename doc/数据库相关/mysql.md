

### 批量更新

```
insert into … on duplicate key update …语句是根据唯一索引判断记录是否重复的；
```

- 如果不存在记录，插入，则影响的行数为1；
- 如果存在记录，可以更新字段，则影响的行数为2；
- 如果存在记录，并且更新的值和原有的值相同，则影响的行数为0。

如果表同时存在多个唯一索引，只会根据第一个在数据库中存在相应value的唯一索引做duplicate判断：

如果表含有auto_increment字段，使用insert … on duplicate key update插入或更新后，last_insert_id()返回auto_increment字段的值。 

https://www.jb51.net/article/280833.htm



```
insert ignore into … on duplicate key update …
```

没有数据，就插入数据；有数据就跳过此条



### 常用函数

```sql
-- 数字转字符
select chr(to_number('1') + 64) from dual;
-- 字串转整数
select CAST('1' AS SIGNED)
select CONVERT('1' SIGNED)
-- 字串转浮点数
select CAST('1.2' AS DECIMAL(9,2))
select CONVERT('1.2' DECIMAL(9,2))
-- 判断正负，正数返回1，负数返回-1，0返回0
select sign(5-1) from dual
-- decode表达式,val为变量，如果为null，则返回0，否则返回val；如果没有最后默认值，返回null
select decode(val,null,0,val)
-- 四舍五入[，保留两位小数]
select round((8-3)/3*100,2) from dual
-- 连接字符串
select '%' || '业务' || '%' from dual
-- 截取字符串 ('string','start','end') ab
select substr('abcdefg',0,2) from dual
-- 匹配多个字符串（查询机构类别包含05或09的机构）
select s.ORG_CATEGORY from WMLMIS.S_ORG s
where REGEXP_LIKE(s.ORG_CATEGORY, '05|09')

-- 列名1多行合并成一行,通过','分隔，通过列名2排序s_org;
select group_concat(org_name order by org_code separator "/") from s_org; 
```



### 备份表

```sq
-- 创建表结构
create table t_itm_param1 like t_itm_param;
-- 插入到数据
insert into t_itm_param1 select * from t_itm_param;
```

### 日期处理

```sql
获取当前日期(yyyy-MM-dd)
select date_format(now(), '%Y-%m-%d');

%W 星期名字
%Y 年，数字，4位
%y 年，数字，2位

```



### 结果集处理

```sql
-- 并集
union
union all

-- 交集
inner join

-- 差集
left join

```



### 查询机构

#### 本级及下级

```sql

```

#### 本级及上级

```sql
select t5.org_code 
from (
    	select _ids,orgCodes 
    	from (
                select @ids _ids,
                        (select @ids := group_concat(suporg_code) 
                        from s_org 
                        where find_in_set(org_code,@ids)) orgCodes
                from (select * from s_org limit 15) t1,
                     (select @ids := 'JXJK0001') t2
            )t3 
    	where _ids is not null
	)t4,
	s_org t5 
where find_in_set(t5.org_code,t4._ids);
```

#### 查询部门的所属机构

```sql

```

#### 查询部门的所属一级部门

```sql

```

更新一级排序 5.7以下

```mysql
select concat('update s_org a
            set a.FIRST_ORDER = (select group_concat(_ids order by r separator ''|'') orgCode
            from (select u.*, (@i := @i + 1) ''r''
            from (
            select t3._ids
            from (
            select t._ids, orgCodes
            from (
            select @ids _ids,
            (select @ids := group_concat(SUPORG_CODE)
            from s_org
            where find_in_set(ORG_CODE, @ids)) orgCodes
            from s_org t1,
            (select @ids := ''', ORG_CODE, ''') t2
            ) t
            where _ids is not null
            ) t3,
            s_org t4
            where find_in_set(t4.ORG_CODE, t3._ids)
            ) u,
            (select @i := 0) as rownum) m ),
            TWO_ORDER = concat(''|'', a.ORG_CODE)
            where a.ORG_CODE = ''', ORG_CODE ,''';') a
from s_org
```



### 授权

```
grant 权限1,权限2... on 数据库名称.表名称 to 用户@用户地址 identified by '连接口令';
```

权限1,权限2... 被 all privileges 或者 all 代替，表示赋予用户全部权限

数据库名称.表名称 被 `*.*` 代替，表示赋予用户操作服务器上所有数据库所有表的权限

用户地址可以是localhost，也可以是ip地址，机器名字，域名。也可以用'%'表示所有地址都可以连接

'连接口令'不能为空，否则创建失败（？这个要试试）。

