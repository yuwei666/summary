# nginx

是一个高性能的web服务器和反向代理服务器，[官网下载](https://nginx.org/en/download.html)

+ web服务器 

  静态页面的web服务器，不支持java语言，需要配合tomcat完成。

+ 反向代理（*）

  客户端对代理无感知，将请求发送到代理服务器，代理服务器去选择目标服务器获取数据后，再返回给客户端。（客户端而言隐藏了目标服务器IP地址 ）

+ 负载均衡（*）

  基于反向代理，将客户端请求分发到多个服务器上，也就是负载到多个服务器上，就是负载均衡

+ 正向代理

  客户端通过代理服务器访问Internet （目标服务器而言隐藏了客户端的真实IP地址 ）

+ 动静分离

  加快网站访问速度，将动态页面和静态页面由不同的服务器进行解析

## 常用指令

重新加载配置文件**（非常常用）**

```
./nginx -s reload
```

启动nginx进程

```
./nginx
```

 查看nginx.conf配置文件的位置以及检查配置文件是否正确 

```
./nginx -t
```

关闭

```nginx
./nginx -s stop
```

#### 常见错误

 端口号被占用

 nginx文件夹路径含中文，能启动，但是访问不了

 其他错误就详细看`logs/error.log`中的描述

 ## 配置

nginx.conf 配置文件有三部分组成，全局块，events块， 代理、缓存和日志定义等绝大多数功能和第三方模块的配置 

配置文件路径 `/nginx/conf/nginx.conf`，精简之后的内容如下 

```nginx
-- 全局块
worker_processes  1;

-- events块
events {
    worker_connections  1024;
}

-- 代理、缓存和日志定义等绝大多数功能和第三方模块的配置 
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   html;
            index  index.html index.htm;
        }
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

### 全局块

会设置一些影响nginx 服务器整体运行的配置指令， 主要包括配 置运行 Nginx 服务器的用户（组）、允许生成的 worker process 数，进程 PID 存放路径、日志存放路径和类型以 及配置文件的引入等。  

这是 Nginx 服务器并发处理服务的关键配置，worker_processes 值越大，可以支持的并发处理量也越多，但是 会受到硬件、软件等设备的制约。 

### events块

events 块涉及的指令主要影响 Nginx 服务器与用户的网络连接。常用的设置包括

+ 是否开启对多 work process 下的网络连接进行序列化，
+ 是否 允许同时接收多个网络连接，选取哪种事件驱动模型来处理连接请求，
+ 每个 word process 可以同时支持的最大连接数等。

上述例子就表示每个 work process 支持的最大连接数为 1024.这部分的配置对 Nginx 的性能影响较大，在实际中应该灵活配置。

 上述例子就表示每个 work process 支持的最大连接数为 1024.
这部分的配置对 Nginx 的性能影响较大，在实际中应该灵活配置。 

### http块

这算是 Nginx 服务器配置中最频繁的部分，代理、缓存和日志定义等绝大多数功能和第三方模块的配置都在这里。 http 块也可以包括 http全局块、server 块。 



#### server

该部分就是针对网站代码的配置，如果我们需要配置多个网站，可以复制配置多个server 

```
    server {
        listen       80;
        listen       somename:8080;  # 可以写其他的网站的端口，但是要能通，不通会报错
        server_name  somename  alias  another.alias;

        location / {
            root   html;
            index  index.html index.htm;
        }
    }
```

##### listen

指定访问该网站的端口号，默认为当前服务器的80接口。

#####  server_name 

配置域名，服务名可以使用一个确切的、通配符或者正则表达式来命名。 

当nginx通过名称来搜索虚拟服务时，如果一个名称匹配到多个指定的服务块，例如，同时匹配上了通配符和正则表达式的，那nginx会按照下面优先级来选择最先匹配到的server块来处理这个请求：

准确的名称 > 以\*号开始最长的通配符命名 > 以\*号结束最长的通配符命名 > 第一个匹配到的正则表达式命名（按照在配置文件出现的先后顺序）

#####  location 

语法：location [ = | ~ | ~* | !~ | !~* | @ ] uri {...}

+ =  表示精确匹配，如果找到，立即停止搜索并立即处理此请求。 √

+ ~  表示执行一个正则匹配，区分大小写匹配

  > ```nginx
  > location ~ ^/jg {		# ^/jg 表示以/jg开头，下同，除了区分大小写
  >     root   html;
  >     index  index.html index.htm;
  > }
  > ```

+ ~*  表示执行一个正则匹配，不区分大小写匹配

+ !~  区分大小写不匹配（相当于不等于）

+ !~* 不区分大小写不匹配 （相当于不等于）

+ ^~  即表示只匹配普通字符（空格）。使用前缀匹配，^表示“非”，即不查询正则表达式。如果匹配成功，则不再匹配其他location。

  >  解释：正常使用~会按照正则匹配，^~ 就不走正则表达式了，直接按照原样匹配，和默认一致，不过优先级更高，仅次于=（待确认）
  >
  > ```nginx
  > location ^~ /a {
  >     root  C:/Users/Thinkpad/Desktop/;
  >     index  index.htm;
  > }
  > ```
  >
  > http://localhost/ab  -> C:/Users/Thinkpad/Desktop/b （自动补/）
  >
  > ```nginx
  > location /a {
  >     root  C:/Users/Thinkpad/Desktop/;
  >     index  index.htm;
  > }
  > ```
  >
  > http://localhost/ab  ->C:/Users/Thinkpad/Desktop/ab

+ @  指定一个命名的location，一般只用于内部重定向请求。例如 error_page, try_files

+ 默认

  ```nginx
  location /a {
      alias  C:/Users/Thinkpad/Desktop/;
      index  index.htm;
  }
  ```

  http://localhost/ab -> C:/Users/Thinkpad/Desktop/b

  ```nginx
  location /a {
      root  C:/Users/Thinkpad/Desktop/;
      index  index.htm;
  }
  ```

  http://localhost/ab -> C:/Users/Thinkpad/Desktop/ab

  **优先级**

  `默认` < `~` = `~*` < `^~` < `=` 

```nginx
location / {
    root   html;
    index  index.html index.htm;
    try_files $uri $uri/ /index.html;
}
```

对于使用 Vue Router 的单页应用，你希望所有未找到的文件请求都重定向到 `index.html`，从而让前端路由处理路由逻辑，[try_files](https://www.jianshu.com/p/d54432c172bb) 这个配置的意思是：

1. 尝试访问请求的 URI 对应的文件（`$uri`）。
2. 如果找不到，尝试访问请求的 URI 对应的目录（`$uri/`）。
3. 如果还是找不到，则重定向到 `index.html`。



+ 路由匹配问题**

  配置中使用root时，当请求url为 http://ip:80/api，会先在文件系统的/codeDir目录下查找文件，因为请求的是api，所以会在/codeDir下查找名为api的目录，然后再进一步在目录中查找 index.html或者 index.htm。

  最终使用的文件路径：/codeDir/api/index.html

  ```nginx
  location /api {
              root   /codeDir;
              index  index.html index.htm;
          }
  ```

  

  配置中使用 alias 时，当请求url为 http://ip:80/api，nginx会把/codeDir 作为 /api 的目录，在此目录下直接查找 index.html或者 index.htm。

  最终使用的文件路径：/codeDir/index.html

  ```nginx
  location /api {
              alias  /codeDir/;	 # 注意这里的斜杠，它表示/codeDir目录  
              index  index.html index.htm;
          }
  ```

+ 对于 root后的路径是否要以/结尾的问题

  在Nginx的配置中，使用`root`指令时，目录路径的末尾是否加`/`（斜杠）是有区别的。这主要影响到Nginx如何拼接请求的路径和`root`指定的目录路径。 

  > 在实测nginx-1.27.2之后，发现root后加不加/，没啥区别，都是以文件夹为根路径，拼接上请求路径
  >
  > ```
  > # 带斜杠/
  > location /test {
  > 	root  C:/Users/Thinkpad/Desktop/test/;
  > 	index  proto.html index.htm;
  > }
  > http://localhost/test/a
  > "C:/Users/Thinkpad/Desktop/test/test/a"
  > # 不带斜杠/
  > location /testb {
  > 	root  C:/Users/Thinkpad/Desktop/testb;
  > 	index  proto.html index.htm;
  > }
  > http://localhost/testb/b
  > "C:/Users/Thinkpad/Desktop/testb/testb/b"
  > ```

##### proxy_pass

后面跟代理的路径，规则如下

location:
    以 / 结尾：保留请求路径的后续部分。
    不以 / 结尾：相对路径会被直接追加到 proxy_pass 的末尾。
proxy_pass:
    以 / 结尾：请求路径的后续部分会被附加到其后。
    不以 / 结尾：请求路径的后续部分会直接拼接到 proxy_pass 的路径后面。
    如果proxy_pass后面没有指定路径，Nginx会将整个路径发送到后端服务器。
    如果proxy_pass后面指定了路径，Nginx会移除location块中的前缀，并将剩余的路径附加到指定的路径后面。

```nginx
location /sa/ {
    proxy_pass  http://local-server/;
    proxy_set_header Host  $host:$proxy_port;
}
```

##### proxy_set_header

允许重新定义或添加字段传递给代理服务器的请求头。该值可以包含文本、变量和它们的组合。

上面代码中，在服务器收到请求后，请求头Header中会存在一条host数据

```
name>host value>localhost:80
```

### upstream块

 该指令是一个组合型指令它描述了一组[服务器](https://cloud.tencent.com/act/pro/promotion-cvm?from_column=20065&from=20065)，这组服务器将会被指令 proxy_pass 和 fastcgi_pass 作为一个单独的实体使用，它们可以将 server 监听在不同的端口，而且还可以同时使用TCP和UNIX套接字监听。服务器可以设置不同的权重，如果没有设置权重，那么默认会将其设置为 1 。 

#### nginx负载均衡应用场景

1. 解决高并发问题，通过分发流量到多个节点，以此拓展整体的并发能力

2. 解决单点故障问题，通过部署多节点，当其中一个节点宕机后，还能保障有其他节点提供服务

#### 负载均衡算法

 Nginx自带5种负载均衡算法，是基于`ngx_stream_upstream_module`模块来实现的，所以语法中主要是利用`upstream`关键字 

##### 轮询（默认）

请求依次分发给不同的节点，第一次给第一个节点，第二次给第二个节点，轮流请求，  如果不显示声明，将会默认该种算法 

```nginx
upstream xxx { 
	server 192.168.244.11; 
	server 192.168.244.12; 
}
```

##### 权重

按照设置的权重配比，将请求转发给节点，比如权重为1:2，就会将1/3的请求分发给节点1，2/3的请求分发给节点2。该方式适用于服务器配置有显著差别的场景 

```nginx
upstream xxx { 
	server 192.168.244.11 weight=1; 
	server 192.168.244.12 weight=2; 
}
```

#####  源地址ip哈希法分发 

根据请求的发起方的ip计算出来的hash值进行分发，这样可以将相同ip的请求分发到固定节点以此实现会话保持、 A/B 测试等应用场景、[分布式缓存](https://so.csdn.net/so/search?q=分布式缓存&spm=1001.2101.3001.7020) 。 但如果客户服务器有多个出口ip的话，这种方式就不再适用 

```nginx
upstream xxx {
    ip_hash;
	server 192.168.244.11; 
	server 192.168.244.12; 
}
```

#####  源地址url哈希法分发 

根据其ing求的url的hash值来分发到后端的服务器，为不同业务做分布式缓存的场景下比较适用 

```nginx
upstream xxx {
    hash $remote_addr;
	server 192.168.244.11; 
	server 192.168.244.12; 
}
```

#####  最小连接数法分发 

 后端服务当前的连接情况，动态的选取其中连接数最少的服务器来处理当前请求 

```nginx
upstream xxx {
    least_conn;
	server 192.168.244.11; 
	server 192.168.244.12; 
}
```

配合location使用

```nginx
location /sa/ {
    proxy_pass  http://local-server/;
    proxy_set_header Host  $host:$proxy_port;
}
```

### 整体配置

附上一个整体配置

```nginx

#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}

http {
    include       mime.types;
    include		  gientech/*.conf;	# 包含进其他配置文件
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] '
                         '"$request" $status $body_bytes_sent '
                         'Actual File: "$request_filename"';
        
	# 打印结果：192.168.168.1 - - [30/Mar/2025:11:05:15 +0800] "GET /pacscloud HTTP/1.1" 304 0 Requested: # # "/pacscloud" -> Actual File: "/usr/share/nginx/html/index.html"
        
        
    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

	charset utf-8;
	
	upstream local-server{
		server localhost:8081;
		server localhost:8082;
	}
	
    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;
		charset utf-8;
		
        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
			try_files $uri $uri/ /index.html;
        }

		# location /zhongdianjinxin/ {
		#	alias  C:/Users/Thinkpad/Desktop/zhongdianjinxin/jianguanzhibiao/prod/;
		#	autoindex on;
        #    autoindex_exact_size on; # 显示文件大小
        #    autoindex_localtime on; # 显示文件时间
		#	charset utf-8;
        #}

		location /jg {
            alias  C:/Users/Thinkpad/Desktop/zhongdianjinxin/jianguanzhibiao/;
            index  proto.html index.htm;
        }

		location /sa/ {
            proxy_pass  http://local-server/;
        }
		
        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    # server {
    #     listen       8000;
    #     listen       somename:8080;
    #     server_name  somename  alias  another.alias;
    # 
    #     location / {
    #         root   html;
    #         index  index.html index.htm;
    #     }
    # }


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}
```



## 项目中的使用

在项目中，需要部署前后端，所以都需要在nginx中配置。

前端项目是直接访问html/index.html，然后跳转到前端项目？？

后端就是做负载



服务器中启动文件位置 /sbin 

文件位置 /etc/nginx



