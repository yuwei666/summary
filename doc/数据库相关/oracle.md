### over函数

over函数简直太香了，在写排名时非常好用。

```sql
-- 排名，并列排名占用名次 nulls last 空置放在最后
rank() over(partition by column order by column desc nulls last) rn
dense_rank() over(partition by column order by column desc) rn -- 排名，并列排名不占名次

-- 行号，去除重复行



```

tips：partition by是分组函数，不过并不会产生唯一的结果集，而是多个结果集依次排列

### 查询

1. oracle查询rownum 只能 <= or <，不支持 > or >=

2. union 连接后的数据，不能直接进行操作，需要再套一层

   union的两个结果集可以直接进行合并，但如果需要排序，则需要再查询一次？为啥呢，先留着吧

3. 分页

```sql
select * from (
         select ROWNUM ROWNUM_, t.*
         from (
                  select *
                  from SIMIS.S_ORG
              ) t
         where ROWNUM <= 100
     )
where ROWNUM_ >= 50
```



### 常用函数

```sql
-- 数字转字符
select chr(to_number('1') + 64) from dual;
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
-- 列名1多行合并成一行,通过','分隔，通过列名2排序
select listagg(列名,',') within group (order by 列名2) from table 
-- 包含某个字符串 like %%

```



### 日期处理

```sql
-- 今年开始时间
select concat(to_Char(sysdate, 'yyyy'), '-01-01 00:00:00') from dual
-- 这个月开始时间
select concat(to_Char(sysdate, 'yyyy-MM'), '-01 00:00:00') from dual
-- 今天开始时间
select concat(to_Char(sysdate, 'yyyy-MM-dd'), ' 00:00:00') from dual
-- n年前开始时间
select concat(to_char(add_months(sysdate + 1, -12*n), 'yyyy-MM-dd'),' 00:00:00') from dual
-- n个月前开始时间
select concat(to_char(add_months(sysdate + 1, -n), 'yyyy-MM-dd'),' 00:00:00') from dual
-- n天前
select concat(to_Char(sysdate - n, 'yyyy-MM-dd'), ' 00:00:00') from dual
-- 当前时间
select to_Char(sysdate, 'yyyy-mm-dd hh24:mi:ss') from dual

-- 导出.dat时，替换回车，务必要起个别名，否则导出出错
select replace('xxx', chr(13)||chr(10), @n) 别名 from dual
```



### 结果集处理

```sql
-- 并集
select org_code from s_org
union
select org_code from s_org_all

-- 并集
select org_code from s_org
union all
select org_code from s_org_all

-- 交集
select org_code from s_org
intersect
select org_code from s_org_all

-- 差集(集合A排除 集合A和B的交集)
select org_code from s_org A
minus
select org_code from s_org_all B

```



### 循环执行

```sql
-- 循环300次
declare 
	temp number := 1;
begin
	loop
		temp := temp + 1;
		-- 业务代码... temp直接使用即可
		exit when temp = 300;
end;
```





### 递归查询机构

#### 本级及下级

```sql
select s.org_code
from s_org s
where s.org_code != #{dutyDeptCode,jdbcType=VARCHAR}
connect by prior s.org_code = s.suporg_code
start with s.org_code = #{dutyDeptCode,jdbcType=VARCHAR}
```

#### 本级及上级

```sql
select s.org_code
from s_org s
connect by  s.org_code = prior s.suporg_code
start with s.org_code = '111111111'
```

#### 查询部门的所属机构

```sql
select t.org_code
from s_org t
where t.flag = '0'
	and ROWNUM = 1
connect by t.org_code = prior t.sup_org
start with t.org_code = #{org_code}
```

#### 查询部门的所属一级部门

```sql
-- 不带有虚拟部门
select s.org_code
from s_org s
where connect_by_isleaf = '1'
connect by s.org_code = prior s.sup_org_code and flag = '1'
start with s.org_code = u.org_no

-- 带有虚拟部门
select org_code
from (
	select ROWNUM, S.ORG_CODE
    FROM (
    	select org_code, sup_org, flag, first_order
        from s_org s
        connect by s.org_code = prior s.sup_org
        start with s.org_code = #{org_code}
    ) S
    	left join s_org s1 on s1.org_code = s.sup_org
    where s.sup_org is not null
    	and s.flag = 1
    	and s1.flag = 0
    order by s.FIRST_ORDER DESC
)
where ROWNUM = 1
```



### 查询人员

查询人员的精髓是：先查出本级下的所有机构和当前部门下的人员，然后在树中点击部门时，传入点击的部门code，会传入从根节点到点击条目的ids，以','分割，传入最后一个id，再次查询此SQL。

```sql
select *
    from (select s.uass_id id, s.user_name name, s.ORG_NO pid, 'user' flag, 'false' isParent, s.USER_SEQ seq
          from S_USER s
          where s.SYN_STATE = '1'
            and s.ORG_NO = #{dutyDeptCode}
          order by s.USER_SEQ)
    union all
    select t.* from (select t.org_code                                            id,
            t.org_name                                            name,
            t.suporg_code                                         pid,
            t.flag,
            (case when t.flag = '0' then 'false' else 'true' end) isParent,
            t.FIRST_ORDER                                         seq
     from s_org t
     where t.SUPORG_CODE = #{dutyDeptCode}
       and t.org_merspl = '0'
       and t.org_state = '01'
       and t.FLAG = '1'
      order by t.FIRST_ORDER
    ) t
```

要保证第一次查询为根节点，需要对传入的id进行分割，如果长度=1则为根，所以进行单独的查询。

```sql
    select t.org_code                                            id,
           t.org_name                                            name,
           t.suporg_code                                         pid,
           t.flag,
           (case when t.flag = '0' then 'false' else 'true' end) isParent,
           t.FIRST_ORDER                                         seq
    from s_org t
    where t.ORG_CODE = #{dutyDeptCode}
```



### 常见错误

**ORA-00918** 列名重复，看是否查询了相同的列名多次，在分页时额外嵌套一次查询，造成列名不明确。



### 建库

（一）

```
cmd sqlplus /nolog

conn /as sysdba

alter user  sys identified by 123456;

请输入用户名:  system as sysdba
输入口令:


create temporary tablespace simis
tempfile 'D:\app\Yuwei\oradata\orcl\simis.dbf'
size 50m
autoextend on 
next 50m maxsize 20480m
extent management local;

create tablespace simis1
datafile 'D:\app\Yuwei\oradata\orcl\simis1.dbf'
size 1024m
autoextend on 
next 50m maxsize 20480m
extent management local;

create user simis identified by simis
default tablespace simis1
temporary tablespace simis;

grant connect,resource,dba to simis;

imp simis/simis@orcl file=H:simis20200211.dmp full=y;

set sqlblanklines on imp simis/simis@orcl file="H:\simis20200211.dmp" full=y ignore=y

set sqlblanklines on imp simis/simis@orcl file="H:\simis2011.dmp" full=y ignore=y

------------------------------------
​```sql
-- 创建表空间
create tablespace WMLMIS datafile 'wmlmispd1.dbf' size 1024M autoextend on next 1024M maxsize 10240M extent management local;
-- 创建临时表空间 文件路径可以更换
create temporary tablespace wmlmistemp tempfile 'wmlmistemp.dbf' size 100M autoextend on next 50M maxsize 2048M extent management local;

--查询所有表空间
select * from Dba_Tablespaces

-- 创建用户	
create user ftlmispd identified by ftlmispd default tablespace LMISL;
-- 给用户添加角色
grant connect,resource,dba to ftlmispd;

-- 出现空表无法导出的情况
-- 1.先进行以下查询 -- 2.将查询结果执行
select 'alter table ' || table_name || ' allocate extent;'
  from user_tables
 where num_rows = 0
    or num_rows is null;
    
导出表数据	username/pwd	owner：所属用户 file:导出的文件
exp ftlmispd/ftlmispd owner=g:ftlmispd file=ftlmis.dmp compress=n
导出远程oracle
exp ftlmispd/ftlmispD@10.205.0.9:11521/lmis00_yen00117_16.ccb.com
导出表结构
exp simspd1/simspd1@10.205.0.9:11521/user owner=ftlmispd file=ftlmis.dmp rows=n;

导入表数据
imp ftlmispd/ftlmispd@orcl file=f:/ftlmisbk.dmp fromuser=ftlmispd touser=ftlmispd log=temp.log ignore=y;

-- 删除用户及关联的数据
drop user ftlmispd cascade;

-- 删除连接
select sid,serial# from v$session where username='WMLMIS';
alter system kill session'sid,serial'		//使用上面的查询结果
```



#### 数据库回滚

```
alter table t_s_flow_chart enable row movement;

select * from T_S_FLOW_CHART as of timestamp to_timestamp('2020-6-28 10:45:00','yyyy-MM-dd hh24:mi:ss');

flashback table T_S_FLOW_CHART to timestamp to_timestamp('2020-6-28 10:45:00','yyyy-MM-dd hh24:mi:ss');
```



#### 数据库设计

```
select a.TABLE_NAME                                                                             表名,
       c.COMMENTS                                                                               表名注释,
       t.COLUMN_NAME                                                                            字段英文名,
       cc.COMMENTS                                                                              字段中文名,
       decode(t.DATA_TYPE, 'VARCHAR2', t.DATA_TYPE || '(' || t.DATA_LENGTH || ')', t.DATA_TYPE) 数据类型,
       decode(t.NULLABLE, 'Y', '是', '否')                                                        是否必填,
       cc.COMMENTS                                                                              备注
from USER_TAB_COLUMNS t
         right join USER_TABLES a  on a.TABLE_NAME = t.TABLE_NAME
         left join USER_TAB_COMMENTS c on t.TABLE_NAME = c.TABLE_NAME
         left join USER_COL_COMMENTS cc on t.TABLE_NAME = cc.TABLE_NAME and t.COLUMN_NAME = cc.COLUMN_NAME
WHERE a.TABLE_NAME = 'LMIS_BANK'
```

```sql
select a.TABLE_NAME           表名,
       c.COMMENTS              表名注释,
       t.COLUMN_NAME            字段英文名,
       REPLACE(t.COLUMN_NAME, '_', '')   字段名大写,
       '..' ||REPLACE(t.COLUMN_NAME, '_', '')     点点字段名大写,
       lower(SUBSTR(REPLACE(INITCAP(t.COLUMN_NAME), '_', ''), 1, 1)) ||
       substr(REPLACE(INITCAP(t.COLUMN_NAME), '_', ''), 2)                                      字段名小写,
       '..' || lower(SUBSTR(REPLACE(INITCAP(t.COLUMN_NAME), '_', ''), 1, 1)) ||
       substr(REPLACE(INITCAP(t.COLUMN_NAME), '_', ''), 2)                                      点点字段名小写,
       cc.COMMENTS                                                                              字段注释,
       decode(t.DATA_TYPE, 'VARCHAR2', t.DATA_TYPE || '(' || t.DATA_LENGTH || ')', t.DATA_TYPE) 数据类型,
       t.NULLABLE                                                                               是否必填
from USER_TAB_COLUMNS t
         right join USER_TABLES a on a.TABLE_NAME = t.TABLE_NAME
         left join USER_TAB_COMMENTS c on t.TABLE_NAME = c.TABLE_NAME
         left join USER_COL_COMMENTS cc on t.TABLE_NAME = cc.TABLE_NAME and t.COLUMN_NAME = cc.COLUMN_NAME
WHERE a.TABLE_NAME = 'T_FORM_RESULT_FIRST' --更改你自己的表名
order by COLUMN_ID
```



#### 远程数据库导出

1.  获取dba权限进行操作
   适用ssh连接oracle所在linux服务器
   使用sysdba权限登陆

   ```
   sqlplus / as  sysdba
   ```

2. 查询表空间USERS所在位置

   ```
   SELECT file_name from dba_data_files where tablespace_name = 'USERS';
   ```

   查询当前用户的表空间

   ```
   select * from USER_USERS
   ```

3. 新增表空间文件，并设置自动扩容（在原文件路径后面加‘_2’）

   ```
   alter tablespace USERS add datafile '+DATA/bosdb/undotbs100_2.dbf' size 1024M autoextend on next 50M maxsize 20480M;
   ```

4. 导出dmp文件（可以在云桌面直接导出）

   ```
   exp simisnew/simisnew@128.192.119.219:11521/BOSDB owner=simisnew file=simisnew.dmp
   ```

   空表无法导出时，将sql查询结果执行

   ```sql
   select 'alter table ' || table_name || ' allocate extent;'
   from user_tables
   where num_rows = 0
    or num_rows is null;
   ```

   存在表分区的情况，则

   ```sql
   select 'alter table ' || t.TABLE_NAME || ' modify partition ' || t.PARTITION_NAME || ' allocate extent;' from USER_TAB_PARTITIONS t
   ```

5. 导入dmp文件

   ```
   imp simis/simis@128.196.236.162:11521/simis0_CDB00044_17 file=simisnew.dmp fromuser=simisnew touser=simis log=temp.log ignore=y;
   ```

   存在Clob类型数据时，如果tablespace不同，则需要更改dmp文件中表空间名称。修改时，使用notepad++打开，编码方式：ANSI。不要使用UTF-8保存，保存后无法导入。

   如需要删除现有表，则

   ```
   select 'drop table ' || table_name || ';' from cat where table_type = 'TABLE';
   ```

   

   记一次天坑！
   
   datagrip查询char(2)时，长度不足时会自动用空格补齐，项目中则不会。