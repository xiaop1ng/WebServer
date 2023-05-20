package com.xiaoping.server;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;


import cn.hutool.core.util.ClassUtil;
import com.xiaoping.annotate.Action;
import com.xiaoping.annotate.Module;
import com.xiaoping.util.Log;

@Module("/say")
public class App {

	@Action
	public void index(Request req, Response res) throws IOException {
		Result rs = new Result();
		rs.msg = "/index";
		res.send(rs);
	}

	@Action(value="/hello")
	public void hello(Request req, Response res) throws IOException {
		Log.m("function hello invoke.");
		Result rs = new Result();
		String word = req.GET("word").toString();
		rs.msg = "hello, " + word;
		res.send(rs);
	}

	public static void main(String[] args) throws SecurityException {
		// Log.isDebug = false;
		Log.i("plz wait a sec...");

		// 基于 BIO 实现
		// Server server = SockerServer.getInstance();
		// 基于 NIO 实现
		Server server = NIOServer.getInstance();
		scanActionsAndUse("com.xiaoping.server", server);
		// use 方法需要在 listen 之前调用，否则不会生效
		server.use("/user/login", App.class, "login");
		server.use("/echo", App.class, "echo");

		// 扫描注解

		// 启动 server
		server.listen(80);

	}

	/**
	 * 将注解形式的 action 注册到 web 容器中
	 * @param packageName
	 * @param server
	 */
	private static void scanActionsAndUse(String packageName, Server server) {
		Set<Class<?>> clazzs = ClassUtil.scanPackageByAnnotation(packageName, Module.class);
		for (Class<?> clazz : clazzs) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				Module module = clazz.getAnnotation(Module.class);
				Action action = method.getAnnotation(Action.class);
				if (action == null) continue;
				String path = module.value() + action.value();
				Log.i("scan: " + path + " " + clazz.getName() + " " + method.getName());
				// 找到了
				server.use(path, clazz, method.getName());
			}
		}

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
