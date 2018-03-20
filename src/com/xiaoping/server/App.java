package com.xiaoping.server;

public class App {
	public static void main(String[] args) {
		System.out.println("plz wait a sec...");
		
		WebServer server = WebServer.getInstance();
		
		server.listen(3000);

	}
}
