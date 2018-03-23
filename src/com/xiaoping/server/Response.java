package com.xiaoping.server;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.xiaoping.util.Log;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class Response {

    private static final int BUFFER_SIZE = 1024;
    private Request request;
    private OutputStream output;

    private static Map<Integer, String> statusMap = null;
    
    private Map<String, String> headers = null;
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
    		statusMap.put(404, "HTTP/1.1 404 Not Found\r\n");
    		statusMap.put(500, "HTTP/1.1 500 Internal Server Error\r\n");
    		statusMap.put(501, "HTTP/1.1 501 Not Implemented\r\n");
    		statusMap.put(502, "HTTP/1.1 502 Bad Gateway\r\n");
    		statusMap.put(503, "HTTP/1.1 503 Service Unavailable\r\n");
    	}
    }
    
    public Response(OutputStream output) {
        this.output = output;
        this.headers = new HashMap<String, String>();
    }
    
    public void setStatus(int status) {
		this.status = status;
    }
    
    public void setContenType (String mimeType) {
    	this.contenType = mimeType;
    	setHeader("Content-Type", mimeType + ";charset=" + this.charset);
    }
    
    public void setCharset(String charset) {
		this.charset = charset;
		setHeader("Content-Type", this.contenType + ";charset=" + charset);
    }
    
    public void setContent(File file) throws IOException {
    	this.contenType = Files.probeContentType( Paths.get(file.getName()) );
    	setHeader("Content-Length", String.valueOf( file.length() ));
    }
    
    public void setContent(String content) {
    	setHeader("Content-Length", String.valueOf( content.length() ));
    }

    public void setHeader(String key, String val) {
    	this.headers.put(key, val);
    }

    public void setRequest(Request request) {
        this.request = request;
    }
    
    private String getStatusString() {
    		return Response.statusMap.get(this.status) == null? Response.statusMap.get(500) : Response.statusMap.get(this.status);
    }
    
    private String getStatusString(int status) {
    	this.status = status;
    	return getStatusString();
    }
    
    private byte[] getResponseHeaderBytes() {
    	String headerString = "";
    	for (String key: headers.keySet()) {
    		if(null != key && null != headers.get(key)) {
    			headerString += key + ": " + headers.get(key) + "\r\n";
    		}
		}
    	String str = getStatusString() + headerString + "\r\n";
    	return str.getBytes();
    }

    private void send(byte[] data) throws IOException {
		output.write(getResponseHeaderBytes());
		output.write(data);
    }
    
    public void send(String str) throws IOException {
    	send(str.getBytes());
    }
    
    public void send(Object obj) throws IOException {
    	Gson gson = new Gson();
    	setContenType("application/json");
    	send(gson.toJson(obj));
    }
    
    public void send(File file) throws IOException {
    	byte[] bytes = new byte[BUFFER_SIZE];
    	FileInputStream fis = null;
    	if( file.exists() && file.isFile() ) {
    		output.write(getResponseHeaderBytes());
    		fis = new FileInputStream(file);
    		int ch = fis.read(bytes, 0, BUFFER_SIZE);
    		while (ch != -1 ) {
    			output.write(bytes, 0, ch);
    			ch = fis.read(bytes, 0, BUFFER_SIZE);
    		}
    		fis.close();
    	}else {
    		// file 不存在或不是一个文件
    		// TODO:这里也可以替换成一个文件
    		setStatus(404);
    		String content = "<html><body><h1>File Not Found</h1></body></html>";
    		setContent(content);
    		send(content);
    	}
    }
    
    public void sendStaticResource() throws IOException {
    	Log.i(request.getUri());
    	String Uri = request.getUri();
    	if(null == Uri || Uri.equals("/") || Uri.equals("")) {
    		Uri = Server.WEB_INDEX;
    	}
        File file = new File(Server.WEB_ROOT, Uri);

    	send(file);

    }
    
    
}