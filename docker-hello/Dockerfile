# 定制的镜像都是基于FROM的镜像，例如 FROM nginx 就是定制需要的基础镜像，后续的操作都是基于nginx。可以理解为在这个镜像里装了一个“系统”，后面的命令都在这个系统中运行。
# 基础镜像，使用 Alpine 操作系统，OpenJDK 8u201
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