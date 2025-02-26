package com.bugbycode.websocket.realtime.endpoint;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.config.AppConfig;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.realtime.handler.MessageHandler;
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
    
    private WorkTaskPool analysisWorkTaskPool;
    
    private KlinesService klinesService;
    
    private KlinesRepository klinesRepository;
    
    public PerpetualWebSocketClientEndpoint(CoinPairSet coinPairSet,MessageHandler messageHandler, 
    		KlinesService klinesService, KlinesRepository klinesRepository, WorkTaskPool analysisWorkTaskPool) {
    	this.coinPairSet = coinPairSet;
    	this.messageHandler = messageHandler;
        this.container = ContainerProvider.getWebSocketContainer();
        this.analysisWorkTaskPool = analysisWorkTaskPool;
        this.klinesService = klinesService;
        this.klinesRepository = klinesRepository;
        try {
            this.connectToServer();
        } catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
    }
    
    private void connectToServer() throws RuntimeException {
    	try {
			this.container.connectToServer(this, new URI(AppConfig.WEBSOCKET_URL + "/ws/" + coinPairSet.getStreamName()));
			logger.info("开始连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("连接websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName() + "成功。");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("websocket服务：" + AppConfig.WEBSOCKET_URL + "，订阅： " + coinPairSet.getStreamName() + "，已断开连接。");
    }

    @OnMessage
    public void onMessage(String message) {
    	this.messageHandler.handleMessage(message, this, klinesService, klinesRepository, this.analysisWorkTaskPool);
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
	
	public String getStreamName() {
		return this.coinPairSet.getStreamName();
	}
	
	public boolean putFinishPair(String pair) {
		return this.coinPairSet.addFinishPair(pair);
	}
	
	public boolean isFinish() {
		return this.coinPairSet.isFinish();
	}
}
