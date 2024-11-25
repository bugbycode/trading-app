package com.bugbycode.websocket.trading.endpoint;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.config.AppConfig;
import com.bugbycode.websocket.trading.handler.APIMessageHandler;

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
	
	private APIMessageHandler messageHandler;
	
	private WebSocketContainer container;
	
	private final String apiKey;
	
	private final String secretKey;

	public TradingWebSocketClientEndpoint(String apiKey, String secretKey, String baseUrl,APIMessageHandler messageHandler) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.messageHandler = messageHandler;
		this.container = ContainerProvider.getWebSocketContainer();
        this.messageHandler.setClient(this);
	}
	
	public void connectToServer() throws RuntimeException {
		if(isClosed()) {
	    	try {
	    		if(this.messageHandler == null) {
	    			throw new RuntimeException("APIMessageHandler is null.");
	    		}
				logger.info("开始连接websocket服务：" + AppConfig.WEBSOCKET_API_URL);
				this.container.connectToServer(this, new URI(AppConfig.WEBSOCKET_API_URL));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
    }
	
	@OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("连接websocket服务：" + AppConfig.WEBSOCKET_API_URL + "成功。");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
    	this.messageHandler.notifyTask();
        logger.info("websocket服务：" + AppConfig.WEBSOCKET_API_URL + "，已断开连接。");
    }

    @OnMessage
    public void onMessage(String message) {
    	this.messageHandler.handleMessage(message);
    }
    
    public void sendMessage(String message) {
    	if(this.session != null && this.session.isOpen()) {
    		this.session.getAsyncRemote().sendText(message);
    	}
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
    	logger.error(throwable.getMessage(), throwable);
    	this.close();
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

	public String getApiKey() {
		return apiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}
	
}
