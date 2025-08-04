补充一个常用的命令，使得在任何地方都可以nginx -t

```bash
# 临时设置别名，会话关闭后失效
alias nginx='/sbin/nginx'

# alias 添加到你用户目录下的配置文件中，永久有效 (如root用户下)
nano /root/.bashrc

# 在文件末尾添加以下内容：
alias nginx='/usr/sbin/nginx'

# 让配置立即生效（不重启也能用）
source /root/.bashrc
```



这是一个conf.d/pacs.conf配置

```nginx
server {
        listen       80;		# 监听80端口
        server_name  172.16.1.108;	# 服务器ip或域名

    	# 注意写法，静态资源后面不加/
        location /pacscloud {
        	# 如果想拼上/pacscloud,使用root， 如果想替换，就使用alias，视情况而定
            alias /app/pacscloud/web/dist/;
        	# $uri 检查请求的路径是否对应一个实际存在的文件，如果存在，直接返回该文件。
        	# $uri/	上一步未找到文件，检查请求的路径是否是一个目录，如果目录存在，Nginx 会默认尝试返回该目录下的索引文件，取						决于 index 指令的配置。
        	# /index.html 如果前两步均未找到匹配的资源，则最终返回 /index.html。
            try_files $uri $uri/ /index.html;
        	# 配置索引文件，上面$uri/的索引文件在这里定义
            index  index.html index.htm;
            
        	# 打印访问文件真实路径，便于调试
            access_log /var/log/nginx/access.log main;
        }
    
		# 注意写法，代理转发后面加/，否则会出现//xxx/getinfo的情况
        location /pacscloud/prod-api/ {
        	# 在反向代理时传递重要的客户端信息给后端服务器，确保后端获取真实的客户端信息，而不是只能看到来自Nginx的请求。
        	# 将客户端的原始Host请求头（包含域名和端口）完整地传递给后端服务器
             proxy_set_header Host $http_host;
        	# 设置X-Real-IP头为客户端的真实IP地址（Nginx直接连接的客户端IP）
             proxy_set_header X-Real-IP $remote_addr;
        	# 类似上一条，将客户端IP存入REMOTE-HOST头（非标准头，部分应用可能使用）
             proxy_set_header REMOTE-HOST $remote_addr;
        	# 追加客户端IP到X-Forwarded-For头
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
             proxy_pass http://localhost:8082/;
             # 打印代理的url，便于调试
             access_log /var/log/nginx/access.log proxy_trace;
        }

    	# 图像下载
    	location /image/ {
             proxy_set_header Host $host:$server_port;
             proxy_set_header X-Real-IP $remote_addr;
             proxy_set_header REMOTE-HOST $remote_addr;
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
             proxy_pass http://localhost:8888/;

             # 加入 CORS 响应头（允许跨域）
             add_header Access-Control-Allow-Origin *;
             add_header Access-Control-Allow-Methods 'GET, POST, OPTIONS';
             add_header Access-Control-Allow-Headers *;
             add_header Access-Control-Expose-Headers 'Content-Disposition';
        }
    
}
```

nginx.conf

``` nginx
user www-data;
worker_processes auto;
pid /run/nginx.pid;

events {
    worker_connections 768;
}

http {
    # 定义日志格式1
    log_format main '$remote_addr - $remote_user [$time_local] '
                  '"$request" $status $body_bytes_sent '
                  'Actual File: "$request_filename"';
    
	# 定义日志格式2
    log_format proxy_trace '$remote_addr - "$request" → '
                         'Proxy: "$scheme://$proxy_host$uri" → '
                         'Time: $upstream_response_time';

    # 访问日志路径及格式
    access_log /var/log/nginx/access.log main;
    # 错误日志路径及级别
    error_log /var/log/nginx/error.log warn;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 1000;
    types_hash_max_size 2048;

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    include /etc/nginx/conf.d/*.conf;
}
```

