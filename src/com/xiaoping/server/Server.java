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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.xiaoping.util.Log;

public class Server {

	// TODO: 这里的配置项应该写到配置文件里面去
	
	// 默认监听 80 端口
	private int port = 80;

	private String host = "0.0.0.0";
	
	private ServerSocket serverSocket = null;

	// 线程池大小
	private static final int THREAD_POLL_SIZE = 10;

	private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POLL_SIZE);

	// 配置默认静态资源文件夹
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "public";
	
	// 配置默认 index 页面
	public static final String WEB_INDEX = File.separator + "index.html";
	
	// 路由和 Method 的映射
	public static Map<String, Method> routerMap = new HashMap<String, Method>();
	
	// 路由和 Method 对应的类 (Context) 的映射
	public static Map<String, Class<?>> ctxMap = new HashMap<String, Class<?>>();
	
	private Server() {};
	
	private static Server webServer = null;
	
	public static Server getInstance() {
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
			try {
				// 这里 serSocket 阻塞住，当有请求进来，会产生一个 socket 对象 
				Socket socket = serverSocket.accept();
				// 使用线程池处理
				executorService.execute(()-> handler(socket));
			} catch (Exception e) {
				continue;
			}
		}
	}

	public void handler(Socket socket) {
		Log.i("当前线程：" + Thread.currentThread().getName());
		InputStream is = null;
		OutputStream os = null;
		try{
			is = socket.getInputStream();
			os = socket.getOutputStream();
			// 从 socket 中取出输入输出流，分别构建请求和响应对象
			Request req = new Request(is);
			Response res = new Response(os);
			res.setRequest(req);

			// uri 匹配来匹配不一样的请求，交给不同 Action 来处理
			String uri = req.getUri();
			Log.m(uri);
			Method routerMethod = routerMap.get(uri);
			// 这里如果请求能和我们的路由匹配上，则不会返回静态资源
			if(null != routerMethod) { // 能匹配到相应的方法来处理该请求
				routerMethod.invoke(ctxMap.get(uri).getDeclaredConstructor().newInstance(), req, res);
			}else { // 尝试返回静态资源
				res.sendStaticResource();
			}
			// TODO: 设置一个 Timeout 时长

		}catch (Exception e) {
			Log.i(e.getMessage());
		} finally {
			// 关闭 socket 对象
			try {
				is.close();
				os.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void listen(int port) {
		this.port = port;
		this.listen();
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
	 * 添加路由匹配规则
	 * @param path 路由匹配字符串
	 * @param clazz 处理该路由的类
	 * @param methodName 对应的方法名
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void use(String path, Class<?> clazz, String methodName) throws NoSuchMethodException, SecurityException {
		Method m = clazz.getMethod(methodName, Request.class, Response.class);
		routerMap.put(path, m);
		ctxMap.put(path, clazz);
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
