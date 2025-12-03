部署fastdfs集群时，使用容器部署https://github.com/qbanxiaoli/fastdfs

**拉取镜像**

```bash
# 切换root用户（建议）
su - 

docker pull qbanxiaoli/fastdfs

# 无法拉取，需要从国内镜像源拉取，再改名
docker pull docker.1ms.run/qbanxiaoli/fastdfs:latest
docker tag docker.1ms.run/qbanxiaoli/fastdfs:latest qbanxiaoli/fastdfs:latest
docker rmi docker.1ms.run/qbanxiaoli/fastdfs:latest
```

**启动容器**

```bash
# 默认当前用户目录下
cd ~
# 拉取脚本
git clone https://qbanxiaoli@github.com/qbanxiaoli/fastdfs.git

cd fastdfs
# 编辑 docker-compose-linux.yml，如部署多台服务器，则多个IP集群用逗号分割(文件内容如下)

# 使用docker-compose.yml启动容器，默认找docker-compose.yml
docker compose -f docker-compose-linux.yml up -d
```

**docker-compose.yml**

```yml
version: '3'
services:
  fastdfs:
    build: .
    image: qbanxiaoli/fastdfs
    # 该容器是否需要开机启动+自动重启。若需要，则取消注释。
    restart: always
    container_name: fastdfs
    environment:
      # nginx服务端口,默认80端口，可修改
      WEB_PORT: 80
      # tracker_server服务端口，默认22122端口，可修改
      FDFS_PORT: 22122
      # storage_server服务端口，默认23000端口，可修改
      STORAGE_PORT: 23000
      # fastdht服务端口，默认11411端口，可修改
      FDHT_PORT: 11411
      # docker所在的宿主机内网地址，默认使用eth0网卡的地址
      IP: 172.16.1.107,172.16.1.106
      # 防盗链配置
      # 是否做token检查，缺省值为false
      CHECK_TOKEN: 0
      # 生成token的有效时长，默认900s
      TOKEN_TTL: 900
      # 生成token的密钥
      SECRET_KEY: FastDFS1234567890
      # token检查失败，返回的本地文件内容，可以通过文件挂载的方式进行修改
      TOKEN_CHECK_FAIL: /etc/fdfs/anti-steal.jpg
    volumes:
      # 将本地目录映射到docker容器内的fastdfs数据存储目录，将fastdfs文件存储到主机上，以免每次重建docker容器，之前存储的文件>就丢失了。
      - /data/fastdfs:/var/local
    # 网络模式为host，可不暴露端口，即直接使用宿主机的网络端口，只适用于linux系统
    network_mode: host
```

**验证**

```bash
# 进入容器
docker exec -it fastdfs /bin/bash

# 测试上传文件
echo "Hello FastDFS!">/tmp/index.html
fdfs_test /etc/fdfs/client.conf upload /tmp/index.html
	# 出现以下信息证明上传成功 
    storage_upload_by_filename
    group_name=group2, remote_filename=M00/00/00/rBABaWj7UaWAcT0RAAAADwrPuXE05.html
    source ip address: 172.16.1.105
    file timestamp=2025-10-24 18:15:01
    file size=15
    file crc32=181385585
	# 直接访问这个url看是否可以访问
    example file url: http://172.16.1.105/group2/M00/00/00/rBABaWj7UaWAcT0RAAAADwrPuXE05.html

# 进入任意一个容器查看状态，正常情况下可以看到多个group1的节点
fdfs_monitor /etc/fdfs/client.conf
	# 出现以下信息证明状态正常
    Group 1:
    group name = group1
        Storage 1:
                    id = 172.16.1.106
                    ip_addr = 172.16.1.106 (172.16.1.106)  ACTIVE
        Storage 2:
                    id = 172.16.1.107
                    ip_addr = 172.16.1.107 (ubuntu.local)  ACTIVE
	
```

此时fdfs集群就已经启动了，容器内启动了，每个容器内都包含tracker，storage，fastdht，nginx，但目前所有启动的节点都使用group1，如果想使用多个group，那就需要在镜像基础上进一步改造了。



**新建文件**

```bash
su -
sudo mkdir -p /opt/fdfs-g2
cd /opt/fdfs-g2
# 粘贴下面文件内容
> start-storage.sh
> docker-compose.yml
```

**start-storage.sh**

```sh
#!/usr/bin/env bash
set -euo pipefail

# ========= 可配环境变量 =========
GROUP_NAME="${GROUP_NAME:-group2}"
TRACKER_SERVERS="${TRACKER_SERVERS:-}"   # 必填，例如 172.16.1.106:22122,172.16.1.107:22122
FDHT_SERVERS="${FDHT_SERVERS:-172.16.1.106:11411,172.16.1.107:11411}"  # 顺序务必与其他节点一致
WEB_PORT="${WEB_PORT:-80}"
STORAGE_PORT="${STORAGE_PORT:-23000}"
FDHT_PORT="${FDHT_PORT:-11411}"          # 仅用于生成 fdht_servers.conf
CHECK_TOKEN="${CHECK_TOKEN:-0}"
TOKEN_TTL="${TOKEN_TTL:-900}"
SECRET_KEY="${SECRET_KEY:-FastDFS1234567890}"
TOKEN_CHECK_FAIL="${TOKEN_CHECK_FAIL:-/etc/fdfs/anti-steal.jpg}"

mkdir -p /var/local/fdfs/storage/data /var/local/fdfs/tracker /var/local/fdht

# ========= 1) 防盗链 http.conf =========
if [ -f /etc/fdfs/http.conf ]; then
  sed -i "s|^http\.anti_steal\.check_token=.*|http.anti_steal.check_token=${CHECK_TOKEN}|" /etc/fdfs/http.conf || true
  sed -i "s|^http\.anti_steal\.token_ttl=.*|http.anti_steal.token_ttl=${TOKEN_TTL}|" /etc/fdfs/http.conf || true
  sed -i "s|^http\.anti_steal\.secret_key=.*|http.anti_steal.secret_key=${SECRET_KEY}|" /etc/fdfs/http.conf || true
  sed -i "s|/home/yuqing/fastdfs/conf/anti-steal.jpg|${TOKEN_CHECK_FAIL}|" /etc/fdfs/http.conf || true
fi

# 解析列表
IFS=',' read -r -a TS <<< "$TRACKER_SERVERS"
IFS=',' read -r -a FS <<< "$FDHT_SERVERS"

if [ ${#TS[@]} -eq 0 ]; then
  echo "ERROR: TRACKER_SERVERS 不能为空，例如 172.16.1.106:22122,172.16.1.107:22122"; exit 1
fi

# ========= 2) storage.conf =========
sconf=/etc/fdfs/storage.conf
[ -f "$sconf" ] || { echo "ERROR: $sconf 不存在"; exit 1; }

# base/path/port
sed -i "s|^base_path=.*|base_path=/var/local/fdfs/storage|" "$sconf"
sed -i "s|^store_path0=.*|store_path0=/var/local/fdfs/storage|" "$sconf"
if grep -q '^group_name=' "$sconf"; then
  sed -i "s|^group_name=.*|group_name=${GROUP_NAME}|" "$sconf"
else
  echo "group_name=${GROUP_NAME}" >> "$sconf"
fi
sed -i "s|^http.server_port=.*|http.server_port=${WEB_PORT}|" "$sconf" || true
sed -i "s|^port=.*|port=${STORAGE_PORT}|" "$sconf"

# 去重/保活
if grep -q '^check_file_duplicate=' "$sconf"; then
  sed -i "s|^check_file_duplicate=.*|check_file_duplicate=1|" "$sconf"
else
  echo "check_file_duplicate=1" >> "$sconf"
fi
if grep -q '^keep_alive=' "$sconf"; then
  sed -i "s|^keep_alive=.*|keep_alive=1|" "$sconf"
else
  echo "keep_alive=1" >> "$sconf"
fi

# include fdht_servers.conf
if grep -q '^##include .*fdht_servers.conf' "$sconf"; then
  sed -i "s|^##include .*fdht_servers.conf|#include /etc/fdht/fdht_servers.conf|" "$sconf"
elif ! grep -q '^#include /etc/fdht/fdht_servers.conf' "$sconf"; then
  echo "#include /etc/fdht/fdht_servers.conf" >> "$sconf"
fi

# trackers
sed -i "/^tracker_server=/d" "$sconf"
for t in "${TS[@]}"; do echo "tracker_server=${t}" >> "$sconf"; done

# ========= 3) mod_fastdfs.conf =========
mconf=/etc/fdfs/mod_fastdfs.conf
[ -f "$mconf" ] || { echo "ERROR: $mconf 不存在"; exit 1; }

# 确保与 storage.conf 对齐
if grep -q '^group_name=' "$mconf"; then
  sed -i "s|^group_name=.*|group_name=${GROUP_NAME}|" "$mconf"
else
  echo "group_name=${GROUP_NAME}" >> "$mconf"
fi
sed -i "s|^url_have_group_name .*|url_have_group_name = true|" "$mconf"
sed -i "s|^store_path0.*|store_path0=/var/local/fdfs/storage|" "$mconf"
sed -i "s|^storage_server_port.*|storage_server_port=${STORAGE_PORT}|" "$mconf"

# （可选）从 tracker 动态加载参数；不存在则添加
if grep -q "^load_fdfs_parameters_from_tracker" "$mconf"; then
  sed -i "s|^load_fdfs_parameters_from_tracker.*|load_fdfs_parameters_from_tracker = true|" "$mconf"
else
  echo "load_fdfs_parameters_from_tracker = true" >> "$mconf"
fi

# trackers
sed -i "/^tracker_server=/d" "$mconf"
for t in "${TS[@]}"; do echo "tracker_server=${t}" >> "$mconf"; done

# ========= 4) client.conf（便于 fdfs_monitor） =========
cconf=/etc/fdfs/client.conf
if [ -f "$cconf" ]; then
  sed -i "s|^base_path=.*|base_path=/var/local/fdfs/storage|" "$cconf" || true
  sed -i "/^tracker_server=/d" "$cconf" || true
  for t in "${TS[@]}"; do echo "tracker_server=${t}" >> "$cconf"; done
fi

# ========= 5) FDHT servers =========
fdht=/etc/fdht/fdht_servers.conf
: > "$fdht"
echo "group_count = ${#FS[@]}" >> "$fdht"
for i in "${!FS[@]}"; do echo "group${i}=${FS[$i]}" >> "$fdht"; done

# ========= 6) Nginx 基本改写 =========
nconf=/usr/local/nginx/conf/nginx.conf
[ -f "$nconf" ] || { echo "ERROR: $nconf 不存在"; exit 1; }

# 端口（用最保守的通配替换首个 listen 行）
if grep -q "listen " "$nconf"; then
  # 仅替换第一处 listen
  awk -v p="$WEB_PORT" '
    BEGIN{done=0}
    {if(!done && $0 ~ /listen[[:space:]]+[0-9]+;/){ sub(/listen[[:space:]]+[0-9]+;/,"listen " p ";"); done=1 } print }
  ' "$nconf" > "$nconf.tmp" && mv "$nconf.tmp" "$nconf"
fi

# 让 /groupX/M00 命中模块（如果是单数字写法则升级为多位）
if grep -q "location ~ /group\\[0-9\\]/M00" "$nconf"; then
  sed -i "s|location ~ /group\\[0-9\\]/M00|location ~ /group[0-9]+/M00|" "$nconf"
fi

# M00 软链兜底
[ -e /var/local/fdfs/storage/data/M00 ] || ln -s /var/local/fdfs/storage/data /var/local/fdfs/storage/data/M00

# ========= 7) 启动 =========
/etc/init.d/fdfs_storaged start
/usr/local/nginx/sbin/nginx || true
/usr/local/nginx/sbin/nginx -s reload || true

# 自检打印（方便一眼看关键项）
echo "=== mod_fastdfs.conf ==="
grep -E "^(group_name|url_have_group_name|store_path0|storage_server_port|tracker_server|load_fdfs_parameters_from_tracker)" "$mconf" || true
echo "=== storage.conf ==="
grep -E "^(group_name|store_path0|port|http.server_port|tracker_server)" "$sconf" || true
echo "=== fdht_servers.conf ==="
cat "$fdht" || true

# 前台持久
tail -F /usr/local/nginx/logs/access.log /usr/local/nginx/logs/error.log
```

**docker-compose.yml**

```yaml
version: '3'
services:
  fdfs-storage-g2: 	# 表示group2
    image: qbanxiaoli/fastdfs
    container_name: fdfs-storage-g2
    restart: always
    network_mode: host
    volumes:
      - /data/fastdfs_g2:/var/local
      - ./start-storage.sh:/start.sh:ro		# 启动使用脚本start-storage.sh
    environment:
      GROUP_NAME: group2		# 这里指定group2,可更改
      TRACKER_SERVERS: 172.16.1.106:22122,172.16.1.107:22122
      FDHT_SERVERS: 172.16.1.106:11411,172.16.1.107:11411   # 顺序与其他节点保持一致
      WEB_PORT: 80				# 对外预览端口可更改
      STORAGE_PORT: 23000
      FDHT_PORT: 11411
      START_FDHTD: "0"			# 容器内不启动FDHTD，若需要启动设置"1"
    entrypoint: ["/bin/bash","/start.sh"]
```

**启动**

```bash
# 
docker compose up -d
```

**验证**

```bash
# 进入容器
docker exec -it fdfs-storage-g2 bash

fdfs_monitor /etc/fdfs/client.conf
	
	server_count=2, server_index=0
	tracker server is 172.16.1.106:22122
	group count: 2
	
	Group 1:
	group name = group1
		Storage 1:
                id = 172.16.1.106
                ip_addr = 172.16.1.106 (172.16.1.106)  ACTIVE
         Storage 2:
                id = 172.16.1.107
                ip_addr = 172.16.1.107 (172.16.1.107)  ACTIVE
	Group 2:
	group name = group2
		Storage 1:
                id = 172.16.1.105
                ip_addr = 172.16.1.105 (imt-ubuntu.local)  ACTIVE
```

**其他验证**

```bash
```

**Nginx**

配置nginx，保证查看时，使用同一个ip:port访问文件（最好还是用域名。。。）

/etc/nginx/conf.d/fastdfs-proxy.conf

```nginx
upstream fdfs_storage_g1 {
    server 172.16.1.106:80 max_fails=3 fail_timeout=30s;
    server 172.16.1.107:80 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

upstream fdfs_storage_g2 {
    server 172.16.1.105:80 max_fails=3 fail_timeout=30s;
    keepalive 16;
}

server {
    listen 8000;
    server_name _;

    location ^~ /group1/M00/ {
        proxy_pass http://fdfs_storage_g1;

        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        proxy_connect_timeout 3s;
        proxy_read_timeout 30s;
        proxy_send_timeout 30s;

        proxy_buffering on;
        proxy_buffers 8 64k;
        proxy_busy_buffers_size 128k;

        proxy_next_upstream error timeout http_500 http_502 http_503 http_504;
        limit_except GET HEAD { deny all; }
    }
   
	location ^~ /group2/M00/ {
        proxy_pass http://fdfs_storage_g2;

        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        proxy_connect_timeout 3s;
        proxy_read_timeout 30s;
        proxy_send_timeout 30s;

        proxy_buffering on;
        proxy_buffers 8 64k;
        proxy_busy_buffers_size 128k;

        proxy_next_upstream error timeout http_500 http_502 http_503 http_504;
        limit_except GET HEAD { deny all; }
    }

    location / { return 404; }
}
```





