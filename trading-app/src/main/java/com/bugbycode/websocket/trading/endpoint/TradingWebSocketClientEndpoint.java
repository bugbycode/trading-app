package com.bugbycode.websocket.trading.endpoint;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.util.StringUtil;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

/**
 * websocket api客户端
 */
@ClientEndpoint
public class TradingWebSocketClientEndpoint {

	private final Logger logger = LogManager.getLogger(TradingWebSocketClientEndpoint.class);
	
	private Session session = null;
	
	private WebSocketContainer container;
	
	private LinkedList<String> recvMessageQueue;
	
	private Map<String,Long> reqTimeMap;
	
	private String baseUrl;

	public TradingWebSocketClientEndpoint(String baseUrl) {
		this.container = ContainerProvider.getWebSocketContainer();
        this.baseUrl = baseUrl;
	}
	
	public void connectToServer() throws RuntimeException {
		if(isClosed()) {
	    	try {
				logger.info("开始连接websocket服务：" + baseUrl);
				this.container.connectToServer(this, new URI(baseUrl));
				waitOpen();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
    }
	
	@OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.recvMessageQueue = new LinkedList<String>();
        this.reqTimeMap = new Hashtable<String, Long>();
        logger.info("连接websocket服务：" + baseUrl + "成功。");
        notifyAllTask();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("websocket服务：" + baseUrl + "，已断开连接。");
        notifyAllTask();
    }

    @OnMessage
    public void onMessage(String message) {
    	this.addMessage(message);
    }
    
    public void sendMessage(JSONObject message) {
    	if(isClosed()) {
    		this.connectToServer();
    	}
    	String id = message.getString("id");
		if(StringUtil.isEmpty(id)) {
			throw new RuntimeException("调用WebsocketAPI时出现错误，错误原因：缺少请求ID");
		}
		
		this.reqTimeMap.put(id, new Date().getTime());
		
		this.session.getAsyncRemote().sendText(message.toString());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
    	logger.error(throwable.getMessage(), throwable);
    	this.close();
    	notifyAllTask();
    }
    
    public void close() {
    	try {
    		if(this.session != null && this.session.isOpen()) {
    			this.session.close();
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public synchronized boolean isClosed() {
    	return !(this.session != null && this.session.isOpen());
    }
    
    private synchronized void notifyAllTask() {
    	this.notifyAll();
    }
    
    private synchronized void addMessage(String message) {
		this.recvMessageQueue.addLast(message);
		this.notifyAllTask();
	}
	
    public synchronized JSONObject read(String reqId) {
		while(this.recvMessageQueue.isEmpty()) {
			long t = this.reqTimeMap.get(reqId);
			long now = new Date().getTime();
			if((now - t) > 15000) {
				this.close();
				throw new RuntimeException("socket服务通信网络故障");
			}
			if(this.isClosed()) {
				throw new RuntimeException("Websocket连接已断开");
			}
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String result = this.recvMessageQueue.removeFirst();
		JSONObject json = new JSONObject(result);
		String id = json.getString("id");
		if(StringUtil.isEmpty(id)) {
			throw new RuntimeException("未知错误，错误的消息：" + result);
		}
		if(!id.equals(reqId)) {
			addMessage(result);
			read(reqId);
		}
		this.reqTimeMap.remove(id);
		return json;
	}
	
    private synchronized void waitOpen() {
		while(isClosed()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
