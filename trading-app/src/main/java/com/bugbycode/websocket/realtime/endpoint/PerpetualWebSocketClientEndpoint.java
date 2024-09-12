package com.bugbycode.websocket.realtime.endpoint;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.config.AppConfig;
import com.bugbycode.websocket.realtime.handler.MessageHandler;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
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
    
    private String streamName;
    
    public PerpetualWebSocketClientEndpoint(String streamName,MessageHandler messageHandler) {
    	this.streamName = streamName;
    	this.messageHandler = messageHandler;
        this.container = ContainerProvider.getWebSocketContainer();
        try {
            this.connectToServer();
        } catch (Exception e) {
			logger.info(e.getLocalizedMessage());
		}
    }
    
    private void connectToServer() throws RuntimeException {
    	try {
			this.container.connectToServer(this, new URI(AppConfig.WEBSOCKET_URL + "/ws/" + streamName));
			logger.info("开始连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + streamName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + streamName + "成功。");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + streamName + "，已断开连接。");
        this.connectToServer();
    }

    @OnMessage
    public void onMessage(String message) {
    	this.messageHandler.handleMessage(message);
    }
    
    public void sendMessage(String message) {
    	if(this.session != null) {
    		this.session.getAsyncRemote().sendText(message);
    	}
    }
}
