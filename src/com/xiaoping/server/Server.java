package com.xiaoping.server;

/**
 * webserver
 */
public interface Server {

    void listen();
    void listen(int port);
    void listen(int port, String host);
    void use(String path, Class<?> clazz, String methodName);
    void close();

}
