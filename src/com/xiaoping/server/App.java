package com.xiaoping.server;


import java.io.IOException;
import com.xiaoping.util.Log;

public class App {
	
	public static void main(String[] args) throws SecurityException {
		//Log.isDebug = false;
		Log.i("plz wait a sec...");
		
//		Server server = SockerServer.getInstance();
		Server server = NIOServer.getInstance();

		// use 方法需要在 listen 之前调用，否则不会生效
		server.use("/user/login", App.class, "login");
		server.use("/echo", App.class, "echo");
		
		// 启动 server
		server.listen(80);

	}
	
	/**
	 * 登录 Api
	 * @param req
	 * @param res
	 * @throws IOException 
	 */
	public void login(Request req, Response res) throws IOException {
		Log.m("function login invoke.");
		Result rs = new Result();
		// TODO: DB 操作
		if( "admin".equals(req.GET("user_name")) && "123456".equals(req.GET("pwd")) ) {
			// login success
			res.send(rs);
		} else {
			// login fail
			rs.setting("error", -1, "fail");
			res.send(rs);
		}
	}

	public void echo(Request req, Response res) throws IOException {
		Log.m("function echo invoke.");
		Result rs = new Result();
		String word = req.GET("word").toString();
		rs.msg = word;
		res.send(rs);
	}
	
	class Result {
		public String type = "success";
		public int err = 0;
		public String msg = "ok";
		
		public Result() {
			super();
		}

		public Result(String type, int err, String msg) {
			super();
			this.type = type;
			this.err = err;
			this.msg = msg;
		}
		
		public void setting(String type, int err, String msg) {
			this.type = type;
			this.err = err;
			this.msg = msg;
		}
		
		
	}
}
