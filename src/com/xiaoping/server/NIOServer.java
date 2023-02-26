package com.xiaoping.server;

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
import java.nio.charset.Charset;
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

    private static NIOServer server = null;

    public static synchronized NIOServer getInstance() {
        return server == null ? new NIOServer() : server;
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
            try (Selector selector = Selector.open()) {
                //把ServerSocketChannel注册到selector，事件为OP_ACCEPT
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                //如果返回的>0，表示已经获取到关注的事件
                while (selector.select() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        //获得到一个事件
                        SelectionKey next = iterator.next();
                        try {
                            // 使用线程池处理
                            executorService.execute(() -> handler(next));
                        } catch (Exception e) {
                            Log.m("发生错误：" + e.getMessage());
                            continue;
                        }

//                        }
                        iterator.remove();
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


    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
//        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);//设置非阻塞模式
        socketChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(1024)); //buffer分配一个缓冲区 大小为1024
    }

    public void handler(SelectionKey key) {
        // 这里会一直进入
        Log.i("当前线程：" + Thread.currentThread().getName());

        try {
            if (key.isAcceptable()) {//接受
                handleAccept(key);

            }
            if (key.isReadable()) {//开始读
                SocketChannel sc = (SocketChannel) key.channel();// SocketChannel 是一个连接到 TCP 网络套接字的通道
                ByteBuffer buffer = (ByteBuffer) key.attachment();//从 SocketChannel读取到的数据将会放到这个 buffer中
                buffer.clear();
                try {
                    if ((sc.read(buffer)) != -1) {
                        buffer.flip();//flip方法将Buffer从写模式切换到读模式
                        String request = Charset.forName("utf-8").newDecoder().decode(buffer).toString(); //将此 charset 中的字节解码成 Unicode 字符
                        Log.m(request);
                        Request req = new Request(request);
                        Response res = new Response(sc);
                        res.setRequest(req);
                        // uri 匹配来匹配不一样的请求，交给不同 Action 来处理
                        String uri = req.getUri();
                        Log.m(uri);
                        Method routerMethod = routerMap.get(uri);
                        // 这里如果请求能和我们的路由匹配上，则不会返回静态资源
                        if (null != routerMethod) { // 能匹配到相应的方法来处理该请求
                            routerMethod.invoke(ctxMap.get(uri).getDeclaredConstructor().newInstance(), req, res);
                        } else { // 尝试返回静态资源
                            res.sendStaticResource();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
//                    sc.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
