### 1. 准备工作

+ 安装OpenSSL：确保系统已安装OpenSSL工具。
+ 创建证书目录：
  ```bash
  sudo mkdir -p /etc/docker/certs
  cd /etc/docker/certs
  ```

### 2. 生成CA证书
+ 生成CA私钥
  ```bash
  openssl genrsa -aes256 -out ca-key.pem 4096
  ```
  输入密码保护私钥，后面都要用到

+ 生成CA根证书：
  ```Bash
  openssl req -new -x509 -days 365 -key ca-key.pem -sha256 -out ca.pem
  ```
  根据提示填写CA信息（如国家、组织等）。

### 3. 生成服务器证书
+ 生成服务器私钥
  ```bash
  openssl genrsa -out server-key.pem 4096
  ```

+ 创建证书签名请求（CSR）
  ```Bash
  openssl req -subj "/CN=$(hostname)" -sha256 -new -key server-key.pem -out server.csr
  ```
  将`$(hostname)`替换成主机名或ip


+ 配置扩展属性：

  创建文件 extfile.cnf，包含服务器IP/DNS（替换IP.x为实际IP或域名）：
  ```Bash
  vim extfile.cnf
  ```ini
  subjectAltName = DNS:yourdomain.com,IP:192.168.1.100,IP:127.0.0.1
  extendedKeyUsage = serverAuth
  ```
  如果没有DNS域名，只保留IP即可  


+ 生成服务器证书：

  ```Bash
  openssl x509 -req -days 365 -sha256 \
    -in server.csr -CA ca.pem -CAkey ca-key.pem \
    -CAcreateserial -out server-cert.pem \
    -extfile extfile.cnf
  ```


### 4. 生成客户端证书
+ 生成客户端私钥：
  ```Bash
  openssl genrsa -out client-key.pem 4096
  ```

+ 创建客户端CSR：
  ```Bash
   openssl req -subj '/CN=client' -new -key client-key.pem -out client.csr
  ```

+ 配置客户端扩展属性：

  创建文件 client-extfile.cnf：
  ```Bash
  vim client-extfile.cnf
  ```
  
  ```ini
  extendedKeyUsage = clientAuth
  ```


+ 生成客户端证书：
  ```bash
  openssl x509 -req -days 365 -sha256 \
    -in client.csr -CA ca.pem -CAkey ca-key.pem \
    -CAcreateserial -out client-cert.pem \
    -extfile client-extfile.cnf
  ```

### 5. 配置Docker守护进程
+ 修改Docker配置：

  编辑 `/etc/docker/daemon.json`，如果没有此文件则新建， 存在文件已经添加过mirrors，则添加：

  ```Bash
  vim /etc/docker/daemon.json
  ```

  ```json
  {
    "registry-mirrors": [
      "https://docker.m.daocloud.io",
      "https://dockerproxy.com",
      "https://docker.mirrors.ustc.edu.cn",
      "https://docker.nju.edu.cn"
    ],
    "hosts": ["tcp://0.0.0.0:2376", "unix:///var/run/docker.sock"],
    "tlsverify": true,
    "tlscacert": "/etc/docker/certs/ca.pem",
    "tlscert": "/etc/docker/certs/server-cert.pem",
    "tlskey": "/etc/docker/certs/server-key.pem"
  }

  ```

+ 重启Docker服务：

    ```bash
    sudo systemctl daemon-reload
    sudo systemctl restart docker
    ```
  此时启动如报错，可能是在Docker的daemon.json配置文件中，hosts字段和默认的Docker服务启动参数冲突会导致启动失败。

  这是因为Docker默认会监听unix:///var/run/docker.sock，而手动指定hosts字段后，Docker会尝试同时监听多个地址，但默认服务配置未正确调整。


+ 报错解决办法：

  + 编辑Docker服务文件
  ```bash
  sudo systemctl edit docker.service
  ```
  + 在文件中添加以下内容，覆盖默认的ExecStart配置：
  
  ```ini
  [Service]
  ExecStart=
  ExecStart=/usr/bin/dockerd
  ```
  这将清除默认的启动参数，使Docker完全依赖daemon.json中的配置。

  + 再次重启Docker服务
  ```bash
  sudo systemctl daemon-reload
  sudo systemctl restart docker
  ```


### 6. 配置客户端
+ 拷贝证书到客户端：
  将以下文件复制到客户端机器（如~/.docker）：
  + ca.pem
  + client-cert.pem
  + client-key.pem

+ 设置环境变量：
  ```Bash
  export DOCKER_HOST=tcp://your-server-ip:2376
  export DOCKER_TLS_VERIFY=1
  ```

### 7. 测试连接
+ 运行测试命令：
  ```bash
  docker --tlsverify version
  ```
  若成功返回版本信息，则TLS配置正确。

### 注意事项
+ 文件权限：
  ```Bash
  chmod 0400 ca-key.pem server-key.pem client-key.pem
  chmod 0444 ca.pem server-cert.pem client-cert.pem
  ```

+ 防火墙：开放端口2376。

+ 证书有效期：定期更新证书（通过调整-days参数）。


