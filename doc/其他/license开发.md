开发模块

为spring web项目添加license功能



如何实现？

因为项目中已有LoginFilter过滤所有请求，没有登录的用户跳转到登录页面，所以不需要对所有请求进行lisence校验，只需要在项目启动时，容器加载完成后加载证书，在用户登录请求时拦截请求校验证书即可。

```
//Spring 中观察者模式实现
public class LicenseCheckListener implements ApplicationListener<ContextRefreshedEvent>{
 @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
    	//获取当前容器的父容器，如果为空则进行证书安装，因为spring项目集成spring mvc，存在父子容器，会多次执行此方法，保证只执行一次
    	ApplicationContext context = event.getApplicationContext().getParent();
        if(context == null){
        	logger.info("++++++++ 开始安装证书 ++++++++");
        	...
        }
    }
}
```



因为只需要在项目启动时解析证书，所以采用了安全性高的非对称加密。在本地使用 keytool 生成公钥和私钥文件，通过私钥和允许访问的ip地址、mac地址等信息生成加密的lisence.lic文件，并把lisence.lic文件和公钥文件放到项目中，在服务器启动时对lisence.lic文件解密安装。



技术方面采用的是

拦截器，spring 的 ApplicationListener，truelicense



遇到什么困难？

遇到的一个问题是，部署的环境是linux和weblogic，文件在部署上的时候，在本地测试没有问题，在服务器上找不到文件，后来发现weblogic在部署时，会把resource下的文件和class文件一起打成jar包，所以无法直接访问jar包里面的文件路径。初步解决方案是不通过文件地址去获取文件对象，而是把文件以流的形式读进内存，但是lisence框架提供的接口里面，并没有接收流的接口，接下来的解决方式是通过流读入内存，再把字节流写入临时文件，最后将临时文件进行解析。最后文件成功对象获取并解析了，但是回过头发现，虽然实现了需求，但是浪费内存和cpu，直接把公钥和license文件放入指定路径下，虽然和项目分离开，但是避免了文件和字节流的转换，更换公钥和license文件也更加方便了。



监听器和过滤器



