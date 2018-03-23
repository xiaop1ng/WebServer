package com.xiaoping.server;

import com.xiaoping.util.Log;

public class App {
	
	public static void main(String[] args) {
		//Log.isDebug = false;
		
		Log.i("plz wait a sec...");
		
		Server server = Server.getInstance();
		
		server.listen(8080);

	}
}
