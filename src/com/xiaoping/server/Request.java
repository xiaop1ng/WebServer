package com.xiaoping.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.xiaoping.util.Log;

public class Request {

	// BIO
	private InputStream input;
	
	// 请求方法 GET|POST
	private String methon;
	
	// 请求 URI 不包含 host 如：127.0.0.1/index.html?p=2 => /index.html
	private String uri;

	// 请求 ? 后面的部分 如：127.0.0.1/index.html?p=2 => p=2
	private String searchString;

	// query 部分的 K-V
	private Map<String, String> queryMap;

	// POST 请求的表单
	private String bodyString;

	// body 部分的 K-V
	private Map<String, String> bodyMap;

	public Request(String request) {
		parseRequestHeader(request.toString());
	}

	public Request(InputStream input) {
		this.input = input;

		StringBuffer request = new StringBuffer(2048);
		int i;
		byte[] buffer = new byte[2048];
		try {
			i = input.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			i = -1;
		}
		for (int j = 0; j < i; j++) {
			request.append((char) buffer[j]);
		}
		// GET请求
		// GET /index.html?a=111222333&a=3333 HTTP/1.1
		// Host: 127.0.0.1:8080
		// Connection: keep-alive
		// Cache-Control: max-age=0
		// Upgrade-Insecure-Requests: 1
		// User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)
		// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36
		// Accept:
		// text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
		// DNT: 1
		// Accept-Encoding: gzip, deflate, br
		// Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
		
		// POST请求
		// POST /index.html HTTP/1.1
		// Content-Type: application/x-www-form-urlencoded
		// cache-control: no-cache
		// User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)
		// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36
		// Accept: */*
		// Host: 127.0.0.1:8080
		// accept-encoding: gzip, deflate
		// content-length: 20
		// Connection: keep-alive
		//
		// username=Jack&passwd=000000
		parseRequestHeader(request.toString());
	}



	/**
	 * 解析请求头
	 */
	private void parseRequestHeader(String requestHead) {
		int idx1, idx2;
		idx1 = requestHead.indexOf(' ');
		if (idx1 != -1) {
			idx2 = requestHead.indexOf(' ', idx1 + 1);
			if (idx2 > idx1) {
				String reqString = requestHead.substring(idx1 + 1, idx2);
				this.methon = requestHead.substring(0, idx1);
				Log.m("req methon: ");
				Log.m(this.methon);
				
				// reqString 中包含 "?" 则存在 query 
				if (reqString.indexOf("?") != -1) {
					String[] reqStringArr = reqString.split("[?]");
					this.searchString = reqStringArr.length > 1 ? reqStringArr[1] : null;
					Log.i(this.searchString);
					if (null != this.searchString) {
						this.queryMap = new HashMap<String, String>();
						String[] getArr = this.searchString.split("&");
						for (int i = 0; i < getArr.length; i++) {
							String kvStr = getArr[i];
							String[] kvArr = kvStr.split("=");
							if (kvArr.length == 2) {
								this.queryMap.put(kvArr[0], kvArr[1]);
							}
						}
					}
					reqString = reqStringArr[0];
				}
				// 从InputStream中读取request信息，并从request中获取uri值
				this.uri = reqString;
				
				// reqString 中包含 Content-Type: application/x-www-form-urlencoded 则存在 body
				// 当然我们还得考虑 Conten-Type: application/json 等 POST 情况 
				// TODO: 补充其他类型的 POST请求
				if (requestHead.indexOf("Content-Type: application/x-www-form-urlencoded") != -1) {
					String[] bodyStringArr = requestHead.split("\r\n\r\n");
					this.bodyString = bodyStringArr.length > 1 ? bodyStringArr[1] : null;
					if (null != this.bodyString) {
						this.bodyMap = new HashMap<String, String>();
						String[] postArr = this.bodyString.split("&");
						for (int i = 0; i < postArr.length; i++) {
							String kvStr = postArr[i];
							String[] kvArr = kvStr.split("=");
							if (kvArr.length == 2) {
								this.bodyMap.put(kvArr[0], kvArr[1]);
							}
						}
					}
				} 
			}

		}

	}

	/**
	 * 获取 GET 参数
	 * @param key
	 * @return
	 */
	public Object GET(String key) {
		return null != this.queryMap ? this.queryMap.get(key) : null;
	}
	
	/**
	 * 获取 POST 参数
	 * @param key
	 * @return
	 */
	public Object POST(String key) {
		return null != this.bodyMap ? this.bodyMap.get(key) : null;
	}

	public String getUri() {
		return uri;
	}

}
