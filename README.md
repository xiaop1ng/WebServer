# WebServer
A Simple WebServer for Java
一个简单的使用 Java 实现的 webServer ， 仅供参考和学习。
实现了简单的静态资源访问以及路由匹配到 Action 处理请求。

# 结构说明

-- src	程序源
	+- server 	CORE包
		-- App.java		程序入口
		-- Server.java	webServer服务的实现
		-- Request.java	请求对象
		-- Response.java	响应对象
	+- util 		工具包 
		-- Log.java		日志打印
		
-- lib	引用第三方库
	-- gson-2.8.0.jar	JSON解析库
	
-- public 静态文件目录	