## 常用

- 使用 `which` 查找可执行文件的路径。
- 使用 `whereis` 查找二进制文件、源代码和手册页。
- 使用 `locate` 快速查找文件（需要更新数据库）。
- 使用 `find` 递归查找文件或目录。
- 使用 `type` 查看命令的类型。



#### 文件操作

##### 查看

文件内容少时，使用cat指令

```bash
#  查看文件内容，-n 显示所有行号  -b 非空输出行号  -s不输出多行空行
cat -n text.txt

# 删除一行
在普通模式下，移动光标，输入dd即可
```



文件内容较多时，使用less、more指令

 `more` 命令允许你按页查看文件内容。当文件内容超过一屏时，`more` 会暂停并显示 `--More--(XX%)`，你可以按空格键查看下一页，或者按 `q` 键退出查看。 

```
more filename
```

less指令更加强大 ，在 `less` 中，你可以使用上下箭头键、Page Up、Page Down 键等来浏览，按 `/` 键进行搜索，按 `n` 键查找下一个匹配项，按 `N` 键查找上一个匹配项，按 `q` 键退出。 

```
less filename
```

实时查看结尾20行的事实日志，并输出到指定文件中

```
tail -f -n 20 xxx.log > temp.log  
```

实时查看头部20行，并输出到指定文件中

##### 查找

 ./和../分别表示当前目录和上一级目录。

```
# 在指定路径下递归查找名称spring-security-开头的文件
find /home/app/. -name "spring-security-*" 

# 过滤掉所有没有权限的提示（不一定有效）
find /home/app/. -name "best_ucsf_model.pt" 2>/dev/null
best_ucsf_model.pt
# 在当前目录下递归查找所有名称为xx的文件
find . -name "xx" 

# 递归查找所有名称xx的文件夹
find . -type d -name "xx" 

# 在根目录下查找文件夹html，并且排出错误选项（例如权限不足）
find / -type d -name html 2>/dev/null
find / -type d -name imagesrv-0.0.1-SNAPSHOT.jar 2>/dev/null
```

```
head -f -n 20 xxx.log > temp.log  
```

##### 查找文件

从文件夹中查询关键字，会从该目录下所有文本中查询此关键字，显示文件名称。



```bash
# 从当前目录下，递归查找文本 text  -r递归  -n 行号
grep -rn "text" .

# 忽略大小写
grep -ril "/pacsiamge" .

# 在置顶目录下查找
grep -rn "/pacsiamge" /path/to/nginx/conf.d

# 注意：grep默认只对普通文件起作用，不加
grep aa
```

##### 读取日志

grep [选项] 关键字 日志文件

```bash
# 从sys.log日志文件中获取前后50行日志
grep -A 数量 "关键字" 文件名
# 从sys.log日志文件中获取前50行日志
grep -B 数量 "关键字" 文件名   
# 从sys.log日志文件中获取后50行日志
grep -C 数量 "关键字" sys.log 
```

配合tail命令

|`：这是一个管道符号，用于将一个命令的输出作为另一个命令的输入。在这里，它将`tail -2000f sys.log`的输出传递给`grep 

```
# 从sys.log文件检索最新2000行日志，然后再从中按照关键字检索后50行日志
tail -2000f sys.log | grep -C 50 '关键字'
```

配合ps命令

 `ps`命令用于显示当前系统的进程状态 , `-e`选项表示显示所有进程，`-f`选项表示以“长格式”显示信息，包括UID、PID、PPID、C、STIME、TTY、TIME和CMD等字段。 

```
ps -ef | grep nginx
```



#### 系统

##### 用户

```bash
# 新建用户 -m：自动创建用户的 home 目录，如 /home/yuwei
sudo useradd -m yuwei
# 添加密码
sudo passwd yuwei
# 授权
usermod -aG wheel yuwei
# 切换用户
su - yuwei
# 退出当前用户
ctrl + d
```



##### 查看进程

```
ps -ef
ps -ef | grep "java"

# 配合下面查看端口占用中的pid
ps -ef pid
```
展示结果中PID 是 进程id

```bash
# 查看占用内存的前10个进程，默认为KB
ps aux --sort=-%mem | head

# 单位展示为MB
ps aux --sort=-%mem | head | awk 'NR==1; NR>1 {$6=int($6/1024)"MB"; print}'

# 展示指定的列
ps aux --sort=-%mem | head | awk 'NR==1 {printf "%-8s %-8s %-8s %-8s %s\n", $1, $2, $4, $6, $11}; NR>1 {$6=int($6/1024)"MB"; printf "%-8s %-8s %-8s %-8s %s\n", $1, $2, $4, $6, $11}'
```
+ 第 5 列（VSZ）：虚拟内存大小（Virtual Memory Size），单位为 KB。

+ 第 6 列（RSS）：常驻内存大小（Resident Set Size），单位为 KB。




##### 查看端口占用

```linux
ss -tulnp #查看所有端口
ss -tulnp | grep :80 #查看80端口    

# 最常用
sudo lsof -nP -iTCP:5432 -sTCP:LISTEN

#使用方式和ss命令一致，新版linux不再支持
netstat -tulnp
```

- t: 显示TCP端口
- u: 显示UDP端口
- l: 显示监听状态的端口
- n: 不解析服务名称（使用端口号）
- p: 显示进程标识符和程序名称
- 形象的记住这个参数： tulnp ：秃驴（tul）拿（n）苹果（p）

##### 杀死进程

```
kill 3781 	# 3781为进程id
kill -9 3781	# kill -9 来强制终止退出
```

##### 实时性能监测

相当于 Windows 系统中的任务管理器。 `ctrl+c` 或 `q`退出

```
top
```

##### 查看cpu信息

```
lscpu
```



##### 复制粘贴

复制 ctrl + insert

粘贴 shift + insert



#### 文件操作

```bash
# 创建文件
touch filename

# 创建文件夹 -p：自动创建上级路径，已存在文件夹不报错
mkdir -p auth 

# 删除文件/文件夹
rm xxx.txt
rm -rf dir

# 授权 -R：递归
chmod -R 777 file

# 移动/重命名
mv file file

# 拷贝
cp  filepath filepath

# 安装
make install

# 解压 .zip
unzip xxx.zip -d target/xxx
```





