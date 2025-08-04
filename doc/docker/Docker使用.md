
### IDEA 使用docker

idea2024以上版本使用docker，tcp连接后，提示

>  com.intellil.execution.process.ProcessNotCreatedException: Cannot run program “docker”:CreateProcess error=2

需要下载本地docker包，[下载](https://download.docker.com/win/static/stable/x86_64/)指定版本的zip，然后解压到任意目录，反选自动检测可执行路径，把两个路径手动指定

```
F:\java\docker\docker.exe (刚才解压的路径)
```

在IDEA中执行docker compose时

```bash
docker compose -f /path/to/your/docker-compose.yml up
```

如果报错`unknown shorthand flag: 'f' in -f`，是因为最新版的docker已经将Docker-Compose作为插件，所以使用docker compose -f docker/docker-compose.yml up -ddocker不能识别，所以后面将Docker-Compose独立版进行删除，并安装Docker-Compose的插件版。

```bash
# 1.检查 Docker Compose 是否是插件版 如果安装的是 Docker Compose 插件而不是独立版，可以通过以下命令检查：
docker compose version
# 如果 docker-compose 命令是通过插件方式运行的，它会返回类似 Docker Compose version X.X.X 的信息。

# 2.卸载独立版 Docker Compose
sudo rm /usr/local/bin/docker-compose

# 3.检查是否已成功卸载：
docker-compose --version

# 4.安装 Docker Compose 插件
sudo apt-get install docker-compose-plugin

# 5.随后通过以下命令检查 Docker Compose 版本
docker compose version
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



### IDEA连接registry

服务工具栏 -> + 添加服务 -> Docker 注册表 -> 注册表选择Docker V2 -> 填写地址 (http://192.168.168.100:5000)、用户名和密码。

使用 HTTP 协议，需要在 Docker 客户端配置信任该仓库：让 Docker 允许访问 **非 HTTPS 的私有镜像仓库（Insecure Registry）**。

编辑 `/etc/docker/daemon.json`：

```bash
{
        "hosts":[
            "unix:///var/run/docker.sock",
            "tcp://0.0.0.0:2375"
        ],
        "registry-mirrors": [
                "https://docker.m.daocloud.io",
                "https://dockerproxy.com",
                "https://docker.mirrors.ustc.edu.cn",
                "https://docker.nju.edu.cn"
        ],
        "insecure-registries": ["http://192.168.168.100:5000"],
        "hosts": ["tcp://0.0.0.0:2376", "unix:///var/run/docker.sock"]
}
```







