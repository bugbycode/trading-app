package com.coinkline.trading_app.task.shape.work;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.coinkline.module.Klines;
import com.coinkline.module.ShapeInfo;
import com.coinkline.repository.shape.ShapeRepository;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.trading_app.pool.WorkTaskPool;

/**
 * 分发绘图任务
 */
public class ShapeDistributeTask implements Runnable {

	private final Logger logger = LogManager.getLogger(ShapeDistributeTask.class);
	
	private final Klines klines;
	
	private final KlinesService klinesService;
	
	private final ShapeRepository shapeRepository;
	
	private final WorkTaskPool analysisWorkTaskPool;
	
	public ShapeDistributeTask(Klines klines,KlinesService klinesService,
			ShapeRepository shapeRepository,WorkTaskPool analysisWorkTaskPool) {
		this.klines = klines;
		this.klinesService = klinesService;
		this.shapeRepository = shapeRepository;
		this.analysisWorkTaskPool = analysisWorkTaskPool;
	}
	
	@Override
	public void run() {
		logger.info("ShapeDistributeTask run start.");
		try {
			List<ShapeInfo> shapeList = shapeRepository.queryBySymbol(klines.getPair());
			if(!CollectionUtils.isEmpty(shapeList)) {
				for(ShapeInfo info : shapeList) {
					this.analysisWorkTaskPool.add(new ShapeAnalysisTask(klines, info, klinesService));
				}
			}
		} catch (Exception e) {
			logger.error("执行分发绘图任务时出现异常", e);
		}
		logger.info("ShapeDistributeTask run end.");
	}

}
