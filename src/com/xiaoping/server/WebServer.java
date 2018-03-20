package com.xiaoping.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer {
	
	//默认监听 80 端口
	private int port = 80;
	
	private String host = "127.0.0.1";
	
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
	
	private WebServer() {};
	private static WebServer webServer = null;
	public static WebServer getInstance() {
		return webServer==null?new WebServer():webServer;
	}
	
	public void listen() {
		System.out.println("WebServer Start,Listen PORT: " + this.port);
		System.out.println("WebServer webroot: " + WEB_ROOT);
		ServerSocket serverSocket = null;
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
                Request request = new Request(is);
                request.parse();
            
                // 创建 Response 对象
                Response response = new Response(os);
                response.setRequest(request);
                response.sendStaticResource();

                // 关闭 socket 对象
                socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public void listen(int port, String host) {
		
		this.port = port;
		this.host = host;
		this.listen();
	}
	
	public void listen(int port) {
		this.port = port;
		this.listen();
	}
}
