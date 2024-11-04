将本地jar包安装到maven库

```
mvn install:install-file "-Dfile=g:/archive-client-1.8.jar" "-DgroupId=com.ccb" "-DartifactId=archive-client" "-Dversion=1.8" "-Dpackaging=jar"
mvn install:install-file "-Dfile=g:/loadbalancer_cache-1.0.3.12.jar" "-DgroupId=com.ccb" "-DartifactId=loadbalancer_cache" "-Dversion=1.0.3.12" "-Dpackaging=jar"
```

```
mvn install:install-file "-Dfile=F:/ojdbc6.jar" "-DgroupId=com.oracle" "-DartifactId=ojdbc6" "-Dversion=11.2.0.1.0" "-Dpackaging=jar"
```

```
mvn install:install-file "-Dfile=F:/ojdbc7.jar" "-DgroupId=com.ojdbc7" "-DartifactId=ojdbc7" "-Dversion=7" "-Dpackaging=jar"
```



一些标签的用法

父模块中：

 该元素下声明的依赖不会实际引入到模块中，只有在 dependencies 元素下同样声明了该依赖，才会引入到模块中。该 元素能够约束 dependencies 下依赖的使用 ，dependencies 声明的依赖若未指定版本，则使用 dependencyManagement 中指定的版本，否则将覆盖 dependencyManagement 中的版本。 

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  
  <groupId>com.youzhibing.account</groupId>
  <artifactId>account-aggregator</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  
  <modules>
　　　 <!-- 模块都写在此处 -->
      <module>account-register</module>
      <module>account-persist</module>
  </modules>

    <!--定义的父类pom.xml 打包类型使pom -->
    <packaging>pom</packaging>
    <dependencyManagement>
        <dependencies>
                <!--导入依赖管理配置-->
                <dependency>
                    <groupId>org.example</groupId>
                    <artifactId>Root</artifactId>
                    <version>1.0</version>
                    <!--依赖范围为 import-->
                    <scope>import</scope>
                    <!--类型一般为pom-->
                    <type>pom</type>
                </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```