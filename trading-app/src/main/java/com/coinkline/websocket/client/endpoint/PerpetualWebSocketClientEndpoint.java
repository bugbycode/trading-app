package com.coinkline.websocket.client.endpoint;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coinkline.config.AppConfig;
import com.coinkline.websocket.client.handler.MessageHandler;
import com.util.CoinPairSet;

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
 * 永续合约实时行情推送websocket客户端
 */
@ClientEndpoint
public class PerpetualWebSocketClientEndpoint {
	
	private final Logger logger = LogManager.getLogger(PerpetualWebSocketClientEndpoint.class);

	private Session session = null;
	
    private MessageHandler messageHandler;
    
    private WebSocketContainer container;
    
    private CoinPairSet coinPairSet;
    
    private ThreadLocal<Integer> coinCount;
    
    public PerpetualWebSocketClientEndpoint(CoinPairSet coinPairSet) {
    	this.coinPairSet = coinPairSet;
    	this.coinCount = ThreadLocal.withInitial(() -> this.coinPairSet.size());
        this.container = ContainerProvider.getWebSocketContainer();
    }
    
    public void setMessageHandler(MessageHandler messageHandler) { 
    	this.messageHandler = messageHandler;
    }
    
    public void connectToServer() throws RuntimeException {
    	try {
    		if(this.messageHandler == null) {
    			throw new RuntimeException("MessageHandler is null.");
    		}
			this.container.connectToServer(this, new URI(AppConfig.WEBSOCKET_URL + "/ws/" + coinPairSet.getStreamName()));
			logger.debug("开始连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.debug("连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName() + "成功。");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.debug("websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName() + "，已断开连接。");
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

	public CoinPairSet getCoinPairSet() {
		return coinPairSet;
	}
	
	public int coinCount() {
		return this.coinCount.get();
	}
	
	public void subCount() {
		this.coinCount.set(this.coinCount() - 1);
	}
	
	public String getStreamName() {
		return this.coinPairSet.getStreamName();
	}
}
