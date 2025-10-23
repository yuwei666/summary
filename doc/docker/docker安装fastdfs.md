#### 背景

fastdfs官方仓库是GitHub 上的 **`happyfish100/fastdfs`**，官方 Wiki 提供了 apt/yum 仓库与源码编译两条路径，依赖清晰、排障简单（systemd 日志、网络直连、磁盘路径清楚）。

该作者并没提供dockerhub的镜像，但是官方仓库含 `docker/dockerfile_local` 与 `docker/dockerfile_network` 示例，可以自己从源码**构建镜像**，这也就是解释了为什么dockerhub中没有fastdfs的docker镜像的原因。



#### 使用

该镜像使用的时候，需要指定host网络，尤其部署分布式存储时，FastDFS **必须**对外上报一个外界可达的 IP；否则 Nginx 模块会转发到容器内网 `172.x` 而失败。

tracker服务器需防火墙开放端口22122

storage服务器需防火墙开放端口23000，8888

```bash
-- 追踪线程
sudo docker run -d \
  --name fastdfs-tracker \
  --network host \
  --restart unless-stopped \
  -v /data/fastdfs/tracker:/var/fdfs \
  -e TZ=Asia/Shanghai \
  delron/fastdfs \
  tracker

-- 可以使用
sudo docker run -d \
  --name fastdfs-storage \
  --network host \
  --restart unless-stopped \
  -v /data/fastdfs/storage:/var/fdfs \
  -e TZ=Asia/Shanghai \
  -e TRACKER_SERVER=172.16.1.106:22122 \
  -e GROUP_NAME=group1 \
  -e STORAGE_IP=172.16.1.109 \
  delron/fastdfs \
  storage
```

#### 进阶

在已安装的 Nginx 上添加 ngx_fastdfs_module，

```
# 下载ngx_fastdfs_module
git clone https://github.com/happyfish100/fastdfs-nginx-module.git

# 下载nginx源码（需与运行的nginx相同版本），解压源码
wget http://nginx.org/download/nginx-1.18.0.tar.gz
tar xf nginx-1.18.0.tar.gz

# 编译
cd nginx-1.18.0
./configure --with-compat --add-dynamic-module=../fastdfs-nginx-module/src
make modules

# 如果报错，请安装以下环境（可选）
sudo apt install -y libpcre3 libpcre3-dev
sudo apt install -y zlib1g zlib1g-dev
```

抱歉，没装上，我是废物。。。。 

现在尝试从https://github.com/happyfish100/fastdfs/wiki安装官方最新版，舍弃容器部署。

泪奔，在Ubuntu20.04装了3天，终于生成了ngx_http_fastdfs_module.so模块，哭死！！！

重点在于，这三个所需要安装的玩意，都必须为同一批次版本（发布时间大致相同），否则直接就是报错。

```bash
切换root用户
su -
mkdir /home/dfs #创建数据存储目录
cd /usr/local/src #切换到安装目录准备下载安装包

# 更新源并安装所需软件
sudo apt-get update
sudo apt install git gcc g++ make automake autoconf libtool pcre2-utils libpcre2-dev zlib1g zlib1g-dev openssl libssh-dev wget vim -y

# 下载三个源码文件
wget https://github.com/happyfish100/fastdfs/archive/refs/tags/V6.06.tar.gz -O fastdfs-6.06.tar.gz
wget https://github.com/happyfish100/fastdfs-nginx-module/archive/refs/tags/V1.22.tar.gz -O fastdfs-nginx-module-1.22.tar.gz
wget https://codeload.github.com/happyfish100/libfastcommon/tar.gz/refs/tags/V1.0.43 -O libfastcommon-1.0.43.tar.gz
wget http://nginx.org/download/nginx-1.18.0.tar.gz

# 解压
tar -zxf fastdfs-6.06.tar.gz
tar -zxf fastdfs-nginx-module-1.22.tar.gz
tar -zxf libfastcommon-1.0.43.tar.gz
tar -zxf nginx-1.18.0.tar.gz

# 解压完成后，/usr/local/src目录下多4个目录：
# fastdfs-6.06 fastdfs-nginx-module-1.22 libfastcommon-1.0.43 nginx-1.18.0

cd libfastcommon-1.0.43/
./make.sh && ./make.sh install
cd ../

cd fastdfs-6.06/
./make.sh && ./make.sh install
# 以下两步便于nginx使用（可选）
cp /usr/local/src/fastdfs-6.06/conf/http.conf /etc/fdfs/ #供nginx访问使用
cp /usr/local/src/fastdfs-6.06/conf/mime.types /etc/fdfs/ #供nginx访问使用
cd ../

# 便于nginx使用（可选）
cp /usr/local/src/fastdfs-nginx-module-1.22/src/mod_fastdfs.conf /etc/fdfs

# 额外安装所需软件
sudo apt-get install -y libpcre3 libpcre3-dev
cd nginx-1.18.0
./configure --with-compat --add-dynamic-module=/usr/local/src/fastdfs-nginx-module-1.22/src/
make modules

# 到这一步，如果顺序完成，则会在nginx-1.18.0中生成文件：
objs/ngx_http_fastdfs_module.so

# 将这个文件复制到modules下
cp objs/ngx_http_fastdfs_module.so /usr/share/nginx/modules/

# 编辑nginx.conf
vim /etc/nginx/nginx.conf

# 第一行加入，如下：
load_module modules/ngx_http_fastdfs_module.so;

user www-data;
worker_processes auto;
pid /run/nginx.pid;
include /etc/nginx/modules-enabled/*.conf;

# 测试
nginx -t
```

到这里，nginx的基本配置就完成了，

剩下的，就是启动fastdfs的过程，同时，也要继续修改nginx配置增加tracker和storage等



```bash
# 追踪线程
sudo docker run -d \
  --name fdfs-tracker \
  --network host \
  --restart unless-stopped \
  -v /data/fdfs/tracker:/var/fdfs \
  -v /data/fdfs/conf/tracker.conf:/etc/fdfs/tracker.conf \
  -e TZ=Asia/Shanghai \
  delron/fastdfs \
  tracker

# 可以使用
sudo docker run -d \
  --name fdfs-storage \
  --network host \
  --restart unless-stopped \
  -v /data/fdfs/storage:/var/fdfs \
  -v /data/fdfs/conf/storage.conf:/etc/fdfs/storage.conf \
  -e TZ=Asia/Shanghai \
  -e TRACKER_SERVER=172.16.1.106:22122,172.16.1.107:22122 \
  delron/fastdfs \
  storage

sudo docker run -d \
  --name fdfs-storage \
  --network host \
  --restart unless-stopped \
  -v /data/fdfs/storage:/var/fdfs \
  -v /data/fdfs/conf/storage.conf:/etc/fdfs/storage.conf \
  -e TZ=Asia/Shanghai \
  -e TRACKER_SERVER=172.16.1.106:22122,172.16.1.107:22122 \
  delron/fastdfs \
  storage
```



修改 `/etc/fdfs/mod_fastdfs.conf`

> `nginx.conf` 里写了：
>
> ```nginx
> load_module modules/ngx_http_fastdfs_module.so;
> ```
>
> 加载module.so时，读取 /etc/fdfs/mod_fastdfs.conf。
>
> 每次 Nginx 启动或 reload 时都会重新读取配置，所以修改此文件后，直接nginx -s reload就可以生效了。

















第三方个人开发者（delron），推荐使用season/fastdfs，/data/fastdfs/storage  挂载的路径需要有足够的空间，空间大小查看

```bash
df -h | grep -v '^tmpfs\|^overlay\|^none'
```

8888端口用于 FastDFS 提供的文件访问服务，23000用于提供文件存储和访问服务的端口

```bash
sudo docker network create fastdfs-net

-- 追踪线程
sudo docker run -d \
  --name fastdfs-tracker \
  --network rld-network \
  --restart unless-stopped \
  -v /var/fastdfs/tracker:/var/fdfs \
  -v tracker_conf:/etc/fdfs \
  -p 22122:22122 \
  -e TZ=Asia/Shanghai \
  -e FASTDFS_PATH=/opt/fdfs \
  -e FASTDFS_BASE_PATH=/var/fdfs \
  -e PORT= \
  -e GROUP_NAME= \
  -e TRACKER_SERVER= \
  -w /tmp/nginx/nginx-1.12.2 \
  delron/fastdfs \
  tracker

-- 可以使用
sudo docker run -d \
  --name fastdfs-storage \
  --network rld-network \
  --restart unless-stopped \
  -v /data/fastdfs/storage:/var/fdfs \
  -p 8888:8888 \
  -p 23000:23000 \
  -e TZ=Asia/Shanghai \
  -e TRACKER_SERVER=fastdfs-tracker:22122 \
  -e GROUP_NAME=group1 \
  -e FASTDFS_PATH=/opt/fdfs \
  -e FASTDFS_BASE_PATH=/var/fdfs \
  -e STORAGE_IP=172.16.1.108 \
  delron/fastdfs \
  storage


```



写在最后：

https://github.com/qbanxiaoli/fastdfs

这个是真神
