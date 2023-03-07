# WebServer
A Simple WebServer for Java
一个简单的使用 Java 实现的 webServer ， 仅供参考和学习。
实现了简单的静态资源访问以及路由匹配到 Action 处理请求。

## 关于 web 服务器

Java 中有很多优秀的 web 服务器（容器），如 Tomcat、Weblogic、JBOSS 等等。我们都知道 web 服务器是用于接受外部请求并给予回应（响应）的一个玩意儿。所以今天造一个可以接受请求并响应请求的轮子，大致思路是使用  `ServerSocket`  对象的  `accept`  方法等待请求进来，有请求进来的时候该方法会返回一个  `Socket`  对象。我们使用  `Socket`  对象的输入输出流来构建请求和响应对象，从而达到响应外部请求的目的。


## 代码清单


```
-- src  程序源
    +- server   CORE
        -- App.java     程序入口
        -- Server.java  webServer服务接口
        -- SockerServer.java webServer服务基于BIO的实现
        -- NIOServer.java webServer服务基于NIO的实现，当前服务选择
        -- Request.java 请求对象
        -- Response.java    响应对象
    +- util         工具包 
        -- Log.java     日志打印

-- lib  引用第三方库
    -- gson-2.8.0.jar   JSON解析库

-- public 静态文件目录    
```

基于BIO的实现的webServer服务，加入线程池来处理请求，在压测 20线程 100次请求中，部分请求在客户端会被拒绝，遂使用NIO的方式实现了`NIOServer`，使用单线程的情况下通过压力测试

## 效果预览

**页面请求**
![静态资源](https://i.loli.net/2019/12/20/d1KzWOqYUk5SAuC.png)

**api 请求**
![API请求](https://i.loli.net/2019/12/20/JuY5wCsgWKPbBAM.png)