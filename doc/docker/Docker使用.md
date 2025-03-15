
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

### Dockerfile

每次执行 Dockerfile 构建镜像时，生成的 sha256: 文件是镜像的层标识符，用于唯一标识镜像层。Docker 使用内容寻址存储（CAS）机制，每个层的内容通过 SHA-256 哈希值来标识。如果层内容未变，Docker 会复用已有的层，否则会生成新的层并分配新的 sha256: 标识符。

可能的原因：
层内容变化：即使 Dockerfile 未变，构建上下文中的文件或依赖项的变化也会导致生成新的层。

时间戳或元数据变化：某些操作（如 COPY 或 ADD）可能会因文件元数据（如时间戳）的变化而生成新层。

构建缓存失效：如果之前的构建缓存失效，Docker 会重新构建层并生成新的 sha256: 标识符。

解决方法：
优化 Dockerfile：减少不必要的层，合并命令，使用 .dockerignore 文件排除不必要的文件。

利用缓存：将不常变化的层放在 Dockerfile 的前面，以充分利用缓存。

检查构建上下文：确保构建上下文中的文件没有不必要的变化。

通过这些方法，可以减少不必要的层生成，从而减少 sha256: 文件的产生。