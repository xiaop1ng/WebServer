package com.xiaoping.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.xiaoping.util.Log;

public class Server {
	
	//默认监听 80 端口
	// TODO: 这里的配置项应该写到配置文件里面去
	private int port = 80;
	
	private String host = "0.0.0.0";
	
	private ServerSocket serverSocket = null;
	
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
	
	public static final String WEB_INDEX = File.separator + "index.html";
	
	public static Map<String, Method> routerMap = new HashMap<String, Method>();
	
	private static App context;
	
	private Server() {};
	
	private static Server webServer = null;
	
	public static Server getInstance(App context) {
		Server.context = context;
		return webServer==null?new Server():webServer;
	}
	
	public void listen() {
		Log.m("WebServer Start,Listen PORT: " + this.port);
		Log.m("WebServer webroot: " + WEB_ROOT);
		try {
			serverSocket = new ServerSocket(this.port, 1, InetAddress.getByName(this.host));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while(true) {
			Socket socket = null;
			InputStream is = null;
			OutputStream os = null;
			
			try {
				socket = serverSocket.accept();
				is = socket.getInputStream();
				os = socket.getOutputStream();
				
				// 创建Request对象并解析
                Request req = new Request(is);

                // 创建 Response 对象
                Response res = new Response(os);
                res.setRequest(req);
                // uri 匹配来匹配不一样的请求，交给不同 Action 来处理
                String uri = req.getUri();
                Log.m(uri);
                Method routerMethod = routerMap.get(uri);
                if(null != routerMethod) {
                		// 能匹配到相应的方法来处理该请求
                		routerMethod.invoke(context, req, res);
                }else {
                		// 尝试返回静态资源
                		res.sendStaticResource();
                }
                // TODO: 设置一个 Timeout 时长
                // 关闭 socket 对象
                socket.close();
			} catch (Exception e) {
				continue;
			}
		}
	}
	
	public void listen(int port) {
		this.port = port;
		this.listen();
	}
	
	/**
	 * 添加路由匹配规则
	 * @param path
	 * @param m
	 */
	public void use(String path, Method m) {
		routerMap.put(path, m);
	}
	
	/**
	 * 启动 Server
	 * @param port
	 * @param host
	 */
	public void listen(int port, String host) {
		this.port = port;
		this.host = host;
		this.listen();
	}
	
	/**
	 * 关闭 Server
	 */
	public void close() {
		if(serverSocket != null && serverSocket.isClosed() == false) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
