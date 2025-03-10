#### 文件操作

##### 查看

文件内容少时，使用cat指令

cat -n text.txt 查看文件内容，-n 显示所有行号 	-b 非空输出行号  -s不输出多行空行

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
find /home/app/. -name "spring-security-*" 2>/dev/null

# 递归查找所有名称为xx的文件
find . -name "xx" 

# 递归查找所有名称xx的文件夹
find . -type d -name "xx" 
```

```
head -f -n 20 xxx.log > temp.log  
```

#### 查找文件



##### 读取日志

grep [选项] 关键字 日志文件

```
// 从sys.log日志文件中获取前后50行日志
grep -A 数量 "关键字" 文件名
// 从sys.log日志文件中获取前50行日志
grep -B 数量 "关键字" 文件名   
// 从sys.log日志文件中获取后50行日志
grep -C 数量 "关键字" sys.log 
```

配合tail命令

|`：这是一个管道符号，用于将一个命令的输出作为另一个命令的输入。在这里，它将`tail -2000f sys.log`的输出传递给`grep 

```
// 从sys.log文件检索最新2000行日志，然后再从中按照关键字检索后50行日志
tail -2000f sys.log | grep -C 50 '关键字'
```

配合ps命令

 `ps`命令用于显示当前系统的进程状态 , `-e`选项表示显示所有进程，`-f`选项表示以“长格式”显示信息，包括UID、PID、PPID、C、STIME、TTY、TIME和CMD等字段。 

```
ps -ef | grep nginx
```



#### 系统

##### 查看进程

```
ps -ef
ps -ef | grep "java"

# 配合下面查看端口占用中的pid
ps -ef pid
```

展示结果中PID 是 进程id

##### 查看端口占用

```linux
ss -tulnp #查看所有端口
ss -tulnp | grep :80 #查看80端口

netstat -tulnp #使用方式和ss命令一致，新版linux不再支持
```

- t: 显示TCP端口
- u: 显示UDP端口
- l: 显示监听状态的端口
- n: 不解析服务名称（使用端口号）
- p: 显示进程标识符和程序名称

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

```
创建文件
touch filename

# 授权 -R：递归
chmod -R 777 file

# 移动/重命名
mv file file

# 拷贝
cp  filepath filepath

# 安装
make install

# 解压 .zip
unzip xxx.zip
```





