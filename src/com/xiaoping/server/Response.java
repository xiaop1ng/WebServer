package com.xiaoping.server;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.xiaoping.util.Log;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class Response {

    private static final int BUFFER_SIZE = 1024;
    private Request request;
    private OutputStream output;

    private static Map<Integer, String> statusMap = null;
    // 默认响应状态 200 OK
    private int status = 200;
    
    private String charset = "utf-8";
    
    private String contenType = "text/html";
    
    static {
    	if(statusMap == null) {
    		statusMap = new HashMap<>();
    		statusMap.put(101, "HTTP/1.1 101 Switching Protocols\r\n");
    		statusMap.put(200, "HTTP/1.1 200 OK\r\n");
    		statusMap.put(201, "HTTP/1.1 201 Created\r\n");
    		statusMap.put(202, "HTTP/1.1 202 Accepted\r\n");
    		statusMap.put(204, "HTTP/1.1 204 No Content\r\n");
    		statusMap.put(300, "HTTP/1.1 300 Multiple Choices\r\n");
    		statusMap.put(301, "HTTP/1.1 301 Moved Permanently\r\n");
    		statusMap.put(302, "HTTP/1.1 302 Moved Temporarily\r\n");
    		statusMap.put(304, "HTTP/1.1 304 Not Modified\r\n");
    		statusMap.put(400, "HTTP/1.1 400 Bad Request\r\n");
    		statusMap.put(401, "HTTP/1.1 401 Unauthorized\r\n");
    		statusMap.put(403, "HTTP/1.1 403 Forbidden\r\n");
    		statusMap.put(504, "HTTP/1.1 404 Not Found\r\n");
    		statusMap.put(500, "HTTP/1.1 500 Internal Server Error\r\n");
    		statusMap.put(501, "HTTP/1.1 501 Not Implemented\r\n");
    		statusMap.put(502, "HTTP/1.1 502 Bad Gateway\r\n");
    		statusMap.put(503, "HTTP/1.1 503 Service Unavailable\r\n");
    	}
    }
    
    public Response(OutputStream output) {
        this.output = output;
        
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            //将web文件写入到OutputStream字节流中
        	Log.i(request.getUri());
            File file = new File(Server.WEB_ROOT, request.getUri());
            
            if (file.exists()) {
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                String header = buildStaticFileHeader(file);
                output.write(header.getBytes());
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
            } else {
                // file not found
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n"
                        + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes());
            }
        } catch (Exception e) {
 
            System.out.println(e.toString());
        } finally {
            if (fis != null)
                fis.close();
        }
    }
    
    
    /**
     * 构建静态文件的响应首标
     * @param file 文件对象
     * @return response header
     * @throws IOException
     */
    public String buildStaticFileHeader(File file) throws IOException {
    		Path path = Paths.get(file.getName());
    		return 	"HTTP/1.1 200 OK\r\n" + 
    				"Content-Type: " + Files.probeContentType(path) + "\r\n" +
    				"Content-Length: " + file.length() + "\r\n" + 
    				"\r\n";
    }
    
}