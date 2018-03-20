package com.xiaoping.server;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class Response {

    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream output;

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
        		System.out.println(request.getUri());
            File file = new File(WebServer.WEB_ROOT, request.getUri());
            
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
            // thrown if cannot instantiate a File object
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