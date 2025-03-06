### Docker使用

定义Dockerfile模板文件，可以让用户很方便的定义一个单独的应用容器。[官方文档](https://docs.docker.com/reference/)



#### Docker常用命令

-- help为帮助 如docker --help 或 docker run --help

##### 系统

```
-- 查看版本
docker version

-- 提交
docker commit

-- 保存，将镜像保存为tar文件
docker save

-- 加载，将tar文件加载为镜像
docker load
```



##### 容器

```
-- 查看所有运行中的容器
docker ps 

-- 查看所有容器(包括已停止的)
docker ps -a
docker ps -aq (只打印id)

-- 容器启动/停止/重启/查看状态       应用名/123为id的前几位，能区分即可
docker start/stop/restart/status nginx/123  

-- 容器删除/强制删除
docker rm nginx/123
docker rm -f nginx/123
-- 强制删除所有容器
docker rm -f $(docker ps -aq)

-- 运行
-- -d表示后台启动 不加-d是在前台启动；
-- --name 表示重命名
-- 81是外部（linux）端口，80是内部（容器内）端口。 外部端口不能重复，容器端口可以。
-- --restart always 开机启动
docker run -d --name myNginx -p 81:80 --restart always nginx

-- -e 设置环境变量

-- run和start的区别
run只在第一次运行时使用，将镜像放到容器中，以后再次启动这个容器时，只需要使用命令docker start即可

-- 查看日志*
docker logs

-- 进入容器
-- -it为交互模式 /bin/bash为命令行模式，可以简写为bash
docker exec -it mynginx /bin/bash
docker exec -it id123 bash
	-- 进入之后可以使用linux命令，如ls，查看文件目录。
	exit离开容器

	# 进入容器后查看文件使用cat命令，其他指令不支持
	cat conf.config

```



##### 镜像

```
# 显示所有镜像（包括中间层镜像）
docker images -a

# 按名称过滤镜像
docker images <镜像名称>
docker images ubuntu

# 查看远程镜像 使用 docker search 命令搜索 Docker Hub 上的镜像：
docker search ubuntu

# 拉取镜像
docker pull <镜像名称>

# 删除镜像
docker rmi <镜像名称或ID>

```



#### 目录挂载

修改容器内的数据，如nginx.conf文件，通过docker exec并不方便。

挂载是在容器运行时，增加 -v 参数，指定容器外部路径挂载到容器内，两边都可以修改

```
docker run -d --name myNginx -p 81:80 **-v /home/yuwei/tmp:/tmp** nginx
```

如果挂载的是nginx.conf所在目录，此时外部如果为空，nginx启动需要读取配置文件，因为挂载的目录以外部为准，所以容器无法启动。应使用卷映射参数运行容器

##### 运行中的容器进行挂载

容器在运行状态下无法直接修改挂载的卷或目录，Docker 的设计决定了卷挂载是在容器启动时确定的，运行时无法动态修改。如果你需要修改挂载，通常需要停止容器并重新创建。 



##### 查看容器的挂载情况

```
docker container inspect idxxx
```

以下是nginx的挂载情况，

```
	"HostConfig": {
            "Binds": [
            	# 将宿主机 /app/nginx/conf/conf.d 目录挂载到容器内的 /etc/nginx/conf.d 目录。
                "/app/nginx/conf/conf.d:/etc/nginx/conf.d",
                # 把宿主机上的 /app/nginx/html 目录挂载到容器内的 /app/ 目录
                "/app/nginx/html:/app/",
                # 将宿主机上的 /app/nginx/logs 目录挂载到容器内的 /var/log/nginx 目录
                "/app/nginx/logs:/var/log/nginx"
            ],
```





#### 卷映射

卷映射和目录挂载是相同参数-v，参数不是以/或.开头的路径，而是自定义卷名，如下方的`ngconf`

```
docker run -d --name myNginx -p 81:80 -v ngconf:/etc/nginx nginx
```

位置统一存储在Linux服务器：`Linux /var/lib/docker/volumes/`\<volumes-name>/_data/

```
-- 查看所有卷
docker volume ls
-- 检查卷
docker volume inspect ngconf
-- 删除卷，可以多个
docker volume rm ngconf xxx
```

##### 总结

宿主机没有容器运行所需文件：

​	卷映射会自动将容器运行所需的文件复制出来，放置到对应的数据卷中。
​	目录挂载，则会导致容器因缺少运行所需的文件，而出错。

宿主机有对应的数据卷或目录：

​	卷映射，就会将容器内的数据覆盖。
​	目录挂载，则会将被容器加载，保证容器的正常运行。

​	对于多容器共享，无论是数据卷挂载还是容器挂载，都可以实现多个容器共享文件。·

卷映射的优点：

​	优点：这种方式相当于把挂载全部交给Docker本身处理，方便快捷。
​	缺点：真实挂载目录由Docker生成，目录较深，不方便查找。

目录挂载的优点：

​	优点：目录和文件更好找也更好维护，并且能直接把目录或者文件挂载到容器上。
​	缺点：目录和文件需要我们自行创建和维护。



#### 网络机制

docker会为每一个启动的容器分配ip，使用命令查看

```
docker inspect mynginx
```

容器内可以使用ip:port互相访问。

但是ip可能会变化，通过创建自定义网络来解决

```
-- 创建网络
docker network create mynet
-- 删除
docker network rm mynet
```

运行容器时，指定网络

```
docker run -d --name myNginx -p 81:80 --network mynet nginx
docker run -d --name myNginx2 -p 82:80 --network mynet nginx
```

这两个容器间，就可以用过容器名称相互访问了。进入容器1后测试访问容器2

```
curl http://myNginx2:80
```



#### 总结

启动一个容器时，考虑是否需要暴露端口，比如-p做端口映射，--network自定义网络；配置文件是否需要-v挂载到外面方便修改；有没有数据需要使用-v挂载到外面方便持久化；容器启动时，需不需要传入环境变量作为初始配置。这些东西去hub.docker.com去镜像说明去查找。



### 分层机制

Docker 镜像是由一系列分层存储（Layered Storage）构成的， 分层存储是 Docker 镜像的核心组成原理之一 。 每个 Docker 镜像由多个层叠加而成，每一层代表一个文件系统的快照。这些层共同构成了一个完整的镜像文件系统。 

每个镜像层都是只读的，当容器运行时，会在镜像层之上再添加一个可写层，用于容器的写操作。这样，多个容器可以共享同一个只读的镜像层，同时拥有各自的可写层，实现资源的高度共享和隔离。 

分层存储的设计使得 Docker 镜像具有可复用性，相同的镜像层可以被多个镜像共享，节省了存储空间，同时降低了镜像的传输时间，提高了镜像的传输效率。 

当我们查看一个镜像的层时，可以看到dockerFile中的每一条指令对应产生的层(Layer)

```
# 查看openjdk:8的镜像历史层
docker image history openjdk:8
-----------------
IMAGE          CREATED       CREATED BY                                       SIZE      COMMENT
b273004037cc   2 years ago   /bin/sh -c set -eux;   arch="$(dpkg --print-…   209MB     
<missing>      2 years ago   /bin/sh -c #(nop)  ENV JAVA_VERSION=8u342        0B        
<missing>      2 years ago   /bin/sh -c #(nop)  ENV LANG=C.UTF-8              0B        
<missing>      2 years ago   /bin/sh -c #(nop)  ENV PATH=/usr/local/openj…   0B        
<missing>      2 years ago   /bin/sh -c { echo '#/bin/sh'; echo 'echo "$J…   27B       
<missing>      2 years ago   /bin/sh -c #(nop)  ENV JAVA_HOME=/usr/local/…   0B        
<missing>      2 years ago   /bin/sh -c set -eux;  apt-get update;  apt-g…   11.3MB    
<missing>      2 years ago   /bin/sh -c apt-get update && apt-get install…   152MB     
<missing>      2 years ago   /bin/sh -c set -ex;  if ! command -v gpg > /…   19MB      
<missing>      2 years ago   /bin/sh -c set -eux;  apt-get update;  apt-g…   10.7MB    
<missing>      2 years ago   /bin/sh -c #(nop)  CMD ["bash"]                  0B        
<missing>      2 years ago   /bin/sh -c #(nop) ADD file:d0f758e50c654c225…   124MB

# 查看基于openjdk:8的镜像的自定义镜像历史层
docker image history docker-hello
-----------------
2bd073165ebd   30 hours ago   ENTRYPOINT ["java" "-Djava.security.egd=file…   0B        buildkit.dockerfile.v0
<missing>      30 hours ago   EXPOSE map[8085/tcp:{}]                          0B        buildkit.dockerfile.v0
<missing>      30 hours ago   ADD target/*.jar app.jar # buildkit              17.1MB    buildkit.dockerfile.v0
<missing>      30 hours ago   VOLUME [/tmp]                                    0B        buildkit.dockerfile.v0
<missing>      30 hours ago   MAINTAINER oink <qq979813679@163.com>            0B        buildkit.dockerfile.v0
<missing>      2 years ago    /bin/sh -c set -eux;   arch="$(dpkg --print-…   209MB     
<missing>      2 years ago    /bin/sh -c #(nop)  ENV JAVA_VERSION=8u342        0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ENV LANG=C.UTF-8              0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ENV PATH=/usr/local/openj…   0B        
<missing>      2 years ago    /bin/sh -c { echo '#/bin/sh'; echo 'echo "$J…   27B       
<missing>      2 years ago    /bin/sh -c #(nop)  ENV JAVA_HOME=/usr/local/…   0B        
<missing>      2 years ago    /bin/sh -c set -eux;  apt-get update;  apt-g…   11.3MB    
<missing>      2 years ago    /bin/sh -c apt-get update && apt-get install…   152MB     
<missing>      2 years ago    /bin/sh -c set -ex;  if ! command -v gpg > /…   19MB      
<missing>      2 years ago    /bin/sh -c set -eux;  apt-get update;  apt-g…   10.7MB    
<missing>      2 years ago    /bin/sh -c #(nop)  CMD ["bash"]                  0B        
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:d0f758e50c654c225…   124MB  
```



### Dockerfile

Dockerfile 是定义和构建 docker 镜像的文本文件，通过编写指令和参数来描述镜像的构建过程和配置，以实现应用程序的打包和部署。

```
# 定制的镜像都是基于FROM的镜像，例如 FROM nginx 就是定制需要的基础镜像，后续的操作都是基于nginx。可以理解为在这个镜像里装了一个“系统”，后面的命令都在这个系统中运行。
# 基础镜像，使用 openjdk:8
FROM openjdk:8

# 自定义标签
LABEL author=oink

# 作者
MAINTAINER oink <qq979813679@163.com>

# 设置环境变量
# ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# 声明一个挂载点，容器内此路径会对应宿主机的某个文件夹
VOLUME /tmp

# 应用构建成功后的 jar 文件被复制到镜像内，名字也改成了 app.jar
ADD target/*.jar app.jar

# 设置 Alpine 系统时区
#ENV TZ=Asia/Shanghai
#RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

# 暴露指定端口
EXPOSE 8085

# 容器启动命令
# 以下命令等同于 java -Djava.security.egd=file:/dev/./urandom -jar /app.jar，推荐使用数组
ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar" ]
```

构建命令

```
# 构建镜像 -f 表示用文件的方式构建 -t 指定名称 .等同于./，代表构建的目录是当前
docker build -f Dockerfile -t 镜像名:tag .
```



### Docker compose

compose 实现了对 Docker 容器集群的快速编排。

一个完整项目势必用到N多个容器（一个容器只运行一个进程）配合完成项目中业务开发，一旦引入N多个容器，容器之间就会形成某种依赖，也就意味某个容器或某些容器的运行需要其他容器优先启动之后才能正常运行。容器的编排显得至关重要，容器的运行一定要有先后顺序。 

Compose 中有两个重要的概念：

- **服务**（service）：一个应用的容器，实际上可以包括若干运行相同镜像的容器实例。
- **项目**(project)：由一组关联的应用容器组成的一个完整业务单元，在 docker-compose.yml 文件中定义。



#### 内容

```
# 版本：已过时，现无需指定
version: "3"

# 可以省略
name: myblog

services:
  # 自定义服务名称
  mysql:
    # 容器名称，不指定默认为服务名
    container_name: mysql
    # 镜像
    image: mysql:8.0
    # 端口
    ports:
      - "3306:3306"
    # 环境变量
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - MYSQL_DATABASE=wordpress
    # 卷
    volumes:
      # 如果使用到了卷映射，需要在顶级元素中声明
      - mysql-data:/var/lib/mysql
      # 路径挂载无需声明
      - /app/myconf:/etc/mysql/conf.d
    # 重启方式：总是
    restart: always
    # 指定网络，可以多个
    networks:
      - blog

  myblog:
    image: wordpress
    ports:
      - "8080:80"
    environment:
      # 同一网络下，主机直接使用 mysql容器名称
      - WORDPRESS_DB_HOST=mysql
      - WORDPRESS_DB_USER=root
      - WORDPRESS_DB_PASSWORD=123456
      - WORDPRESS_DB_NAME=wordpress
    volumes:
      - wordpress:/var/www/html
    restart: always
    networks:
      - blog
    depends_on:
      - mysql

# 声明网络
networks:
  blog:

# 声明卷名，对应上面的卷映射
volumes:
  mysql-data:
  wordpress:
```

使用`docker compose -f docker-compose.yml up -d` 执行结果如下

```
 ✔ Network myblog_blog         Created     0.1s 
 ✔ Volume "myblog_wordpress"   Created     0.0s 
 ✔ Volume "myblog_mysql-data"  Created     0.0s 
 ✔ Container mysql             Started     1.3s 
 ✔ Container myblog-myblog-1   Started 	   1.1s
```



#### 常用命令

```
-- 所有服务上线 -d 后台, 不指定file，默认为compose.yml
docker compose -f docker-compose.yml up -d

-- 所有服务下线，不删除卷，下次启动时保留数据
docker compose down

-- 所有下线时删除image和卷
docker compose down --rmi all -v

-- 启动/停止服务 服务名称
docker compose start/stop name1 name2 

-- 扩容 服务1 × 3，如之前1个实例，现在启动3个实例
docker compose scale name1=3
```



#### 更新

如果对compose.yml中的内容有修改，将wordpress的8080端口改为8081，保存后直接执行上面命令，如果如下

```
docker compose -f docker-compose.yml up -d
[+] Running 2/2
 ✔ Container mysql            Running                                     0.0s 
 ✔ Container myblog-myblog-1  Started                                     1.7s 
```

发现docker只会对修改的容器进行修改。







### IDEA 使用docker

idea2024以上版本使用docker，tcp连接后，提示

>  com.intellil.execution.process.ProcessNotCreatedException: Cannot run program “docker”:CreateProcess error=2 

需要下载本地docker包，[下载](https://download.docker.com/win/static/stable/x86_64/)指定版本的zip，然后解压到任意目录，反选自动检测可执行路径，把两个路径手动指定

```
F:\java\docker\docker.exe (刚才解压的路径)
```



写完dockerfile后，部署到docker，报错

> Deploying 'demo-container Dockerfile: hello/Dockerfile'…
> ERROR: BuildKit is enabled but the buildx component is missing or broken.
>        Install the buildx component to build images with BuildKit:
>        https://docs.docker.com/go/buildx/
> Failed to deploy 'demo-container Dockerfile: hello/Dockerfile': Image build failed with exit code 1.

需要下载buildx [下载i地址](https://github.com/docker/buildx/releases/)，选择.exe版本，移动到 C:\Users\Thinkpad\\.docker\cli-plugins 目录下，改名docker-buildx.exe，再在idea中运行即可。

### 附录

#### 如何修改容器内的端口映射

- 删除容器，重新创建容器，并指定端口映射
- 通过容器配置文件修改端口映射
- 通过 docker commit 将容器构建为一个全新的镜像，然后再通过该镜像创建新的容器，并指定端口映射



#### 安装docker Host

需要安装在Linux环境，https://blog.csdn.net/m0_74825614/article/details/144771695



虚拟机内，允许tcp连接docker，如下

```
sudo vim /etc/docker/daemon.json
```

```
{
        "hosts":[
                "unix:///var/run/docker.sock",
                "tcp://0.0.0.0:2375"
        ],
        ...
}

```

```
停止
sudo systemctl start docker

停止
sudo systemctl stop docker
```



#### 修改仓库源

https://hub.docker.com为官方镜像源，国内使用需要翻墙或替换成其他镜像源。

```
vim /etc/docker/daemon.json
{
        "hosts":[
                "unix:///var/run/docker.sock",
                "tcp://0.0.0.0:2375"
        ],
        "registry-mirrors": ["https://hub.docker.com"]
}

# 保存后重启docker
 sudo systemctl daemon-reload && sudo systemctl restart docker 
```



如果不生效，可以使用域名/img的方式，"docker.domys.cc"为镜像源域名

```
docker pull docker.domys.cc/elasticsearch:7.17.28
```



附： docker 部署gitlab时，clone地址中ip部分不是实际的ip地址，需要进入容器中，执行下面命令

```shell
# 修改配置文件
cd /opt/gitlab/embedded/service/gitlab-rails/config
vi gitlab.yml
-- 修改host内容 ->
  gitlab:
    ## Web server settings (note: host is the FQDN, do not include http://)
    host: 47.94.9.234:800
    port: 80
    https: false
---------------->

# 重启服务
gitlab-ctl restart
```



 



