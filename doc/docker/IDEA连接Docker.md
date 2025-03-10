
### IDEA 使用docker

idea2024以上版本使用docker，tcp连接后，提示

>  com.intellil.execution.process.ProcessNotCreatedException: Cannot run program “docker”:CreateProcess error=2

需要下载本地docker包，[下载](https://download.docker.com/win/static/stable/x86_64/)指定版本的zip，然后解压到任意目录，反选自动检测可执行路径，把两个路径手动指定

```
F:\java\docker\docker.exe (刚才解压的路径)
```



### IDEA 连接Docker TLS
+ 将`etc/docker/certs`路径下三个文件下载
    + ca.pem
    + client-cert.pem
    + client-key.pem

  保存到`C:\Users\Thinkpad\.docker\docker-certs`目录下，后两个文件名称去掉前缀 `client-`，即`ca.pem`、`cert.pem`、`key.pem`三个文件


+ 设置->Docker中，选择 'Tcp Socket'
    + 'Engine API URL':'https://47.94.9.234:2376'
    + 'Certificates folder':'C:\Users\Thinkpad\.docker\docker-certs'

  
