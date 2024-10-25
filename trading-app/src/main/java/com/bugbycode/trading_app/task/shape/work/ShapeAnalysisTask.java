package com.bugbycode.trading_app.task.shape.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Klines;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.shape.ShapeType;
import com.bugbycode.service.klines.KlinesService;
import com.util.EmailUtil;

/**
 * 绘图分析任务
 */
public class ShapeAnalysisTask implements Runnable{

	private final Logger logger = LogManager.getLogger(ShapeAnalysisTask.class);
	
	private final Klines klines;
	
	private final ShapeInfo info;
	
	private final KlinesService klinesService;
	
	public ShapeAnalysisTask(Klines klines, ShapeInfo info, KlinesService klinesService) {
		this.klines = klines;
		this.info = info;
		this.klinesService = klinesService;
	}

	@Override
	public void run() {
		logger.info("ShapeAnalysisTask run start.");
		try {
			//区分绘图类型
			ShapeType type = info.getShapeType();
			logger.info(klines.getPair() + " " + type.getMemo());
			//平行射线
			if(type == ShapeType.LINE_TOOL_HORZ_RAY) {
				klinesService.horizontalRay(klines, info);
			} else if(type == ShapeType.LINE_TOOL_RECTANGLE) {
				klinesService.rectangle(klines, info);
			} else if(type == ShapeType.LINE_TOOL_RAY) {
				klinesService.ray(klines, info);
			} else if(type == ShapeType.LINE_TOOL_PARALLEL_CHANNEL) {
				klinesService.parallelChannel(klines, info);
			} else if(type == ShapeType.LINE_TOOL_TRIANGLE_PATTERN) {
				klinesService.trianglePattern(klines, info);
			} else if(type == ShapeType.LINE_TOOL_RISK_REWARD_LONG) {
				klinesService.riskRewardLong(klines, info);
			} else if(type == ShapeType.LINE_TOOL_RISK_REWARD_SHORT) {
				klinesService.riskRewardShort(klines, info);
			}
		} catch (Exception e) {
			logger.error("执行绘图分析任务时出现异常", e);
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		}
		logger.info("ShapeAnalysisTask run end.");
	}

}
