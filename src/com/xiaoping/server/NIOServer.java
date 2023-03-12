package com.xiaoping.server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.xiaoping.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOServer implements Server {


    // TODO: 这里的配置项应该写到配置文件里面去

    // 默认监听 80 端口
    private int port = 80;

    private String host = "0.0.0.0";

    private ServerSocketChannel serverSocketChannel = null;

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

    private NIOServer() {
    }

    ;

    private static volatile NIOServer server = null;

    public static NIOServer getInstance() {
        if (server == null) {
            synchronized(NIOServer.class) {
                if (server == null) {
                    server = new NIOServer();
                }
            }
        }
        return server;
    }

    @Override
    public void listen() {
        Log.m("WebServer Start,Listen PORT: " + this.port);
        Log.m("WebServer webroot: " + WEB_ROOT);
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //得到Selector对象
            Selector selector = Selector.open();
            //把ServerSocketChannel注册到selector，并说明让Selector关注的点，这里是关注建立连接这个事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                try {
                    selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 获取到 selector 里所有就绪的SelectedKey实例，每将一个channel注册到一个selector就会产生一个selectedKey
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    //获得到一个事件
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    try {
                        if (!key.isValid()) {
                            continue;
                        }
                        handler(key);
                    } catch (Exception e) {
                        Log.m("发生错误：" + e.getMessage());
                        continue;
                    }

                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void handler(SelectionKey key) {
        try {
            // SelectedKey 处于Acceptable状态
            if (key.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel)key.channel();
                // 接受客户端的连接
                SocketChannel client = server.accept();
                if (client == null) {
                    Log.i("No connection is available. Skipping selection key");
                    return;
                }
                // 设置非阻塞模式
                client.configureBlocking(false);
                // 向selector注册socketchannel，主要关注读写，并传入一个ByteBuffer实例供读写缓存
                client.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(1024));
            }
            // SelectedKey 处于可读的状态
            else if (key.isReadable()) {
                // SocketChannel 是一个连接到 TCP 网络套接字的通道
                SocketChannel client = (SocketChannel) key.channel();
                // 从 SocketChannel读取到的数据将会放到这个 buffer 中
                ByteBuffer output = (ByteBuffer) key.attachment();

                if(output == null) {
                    key.attach(output);
                    return;
                }
                // 循环将通道数据读入缓冲区
                while(client.read(output)>0){

                }
                output.flip();
                // 切换到写模式
                key.interestOps(SelectionKey.OP_WRITE);
            }
            // SelectedKey 处于可写的状态
            else if (key.isWritable()) {
                // SocketChannel 是一个连接到 TCP 网络套接字的通道
                SocketChannel client = (SocketChannel) key.channel();
                // 从 SocketChannel读取到的数据将会放到这个 buffer 中
                ByteBuffer output = (ByteBuffer) key.attachment();

                String request = StandardCharsets.UTF_8.decode(output).toString();
                output.flip();
                Log.m(request);
                if (null == request || "".equals(request)){
                    throw new NullPointerException("req is null");
                }
                Request req = new Request(request);
                Response res = new Response(client);
                res.setRequest(req);
                // uri 匹配来匹配不一样的请求，交给不同 Action 来处理
                String uri = req.getUri();
                Log.m("uri:"  + uri);
                Method routerMethod = routerMap.get(uri);
                // 这里如果请求能和我们的路由匹配上，则不会返回静态资源
                if (null != routerMethod) { // 能匹配到相应的方法来处理该请求
                    routerMethod.invoke(ctxMap.get(uri).getDeclaredConstructor().newInstance(), req, res);
                } else { // 尝试返回静态资源
                    res.sendStaticResource();
                }
                // 将以编写的数据从缓存中移除
                output.compact();
            }
        } catch (Exception e) {
            key.cancel();
            try {
                key.channel().close();
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public void listen(int port) {
        this.port = port;
        this.listen();
    }


    /**
     * 启动 Server
     *
     * @param port
     * @param host
     */
    @Override
    public void listen(int port, String host) {
        this.port = port;
        this.host = host;
        this.listen();
    }

    /**
     * 添加路由匹配规则
     *
     * @param path       路由匹配字符串
     * @param clazz      处理该路由的类
     * @param methodName 对应的方法名
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    @Override
    public void use(String path, Class<?> clazz, String methodName) {
        Method m = null;
        try {
            m = clazz.getMethod(methodName, Request.class, Response.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        routerMap.put(path, m);
        ctxMap.put(path, clazz);
    }

    /**
     * 关闭 Server
     */
    @Override
    public void close() {

    }

}
