package com.xiaoping.server;


import java.io.IOException;
import java.lang.reflect.Method;

import com.xiaoping.util.Log;

public class App {
	
	public static void main(String[] args) {
		//Log.isDebug = false;
		
		Log.i("plz wait a sec...");
		
		Server server = Server.getInstance(new App());
		// use 方法需要在 listen 之前调用，否则不会生效
		server.use("/user/login", get("login"));
		
		server.listen(8080);

	}
	
	public static Method get(String methodName) {
		try {
			return App.class.getMethod(methodName, Request.class,Response.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 登录接口
	 * @param req
	 * @param res
	 * @throws IOException 
	 */
	public void login(Request req, Response res) throws IOException {
		Log.m("login invoke");
		Result rs = new Result();
		if( "admin".equals(req.GET("user_name")) && "123456".equals(req.GET("pwd")) ) {
			res.send(rs);
		} else {
			rs.type = "error";
			rs.err = -1;
			rs.msg = "fail";
			res.send(rs);
		}
	}
	
	class Result {
		public String type = "success";
		public int err = 0;
		public String msg = "ok";
	}
}
