package com.bugbycode.trading_app.task.sync.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.util.DateFormatUtil;
import com.util.PriceUtil;

import java.util.List;

/**
 * 分析K线任务
 */
public class AnalysisKlinesTask implements Runnable{

    private final Logger logger = LogManager.getLogger(AnalysisKlinesTask.class);

    private String pair;

    private KlinesService klinesService;

    private KlinesRepository klinesRepository;

    public AnalysisKlinesTask(String pair, KlinesService klinesService, KlinesRepository klinesRepository){
        this.pair = pair;
        this.klinesService = klinesService;
        this.klinesRepository = klinesRepository;
    }

    @Override
    public void run() {
        try {
            //查询日线级别K线信息
            List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
            if(CollectionUtils.isEmpty(klines_list_1d)){
                logger.info("无法获取" + pair + "交易对日线级别K线信息");
                return;
            }
            
            //检查更新
            if(klinesService.verifyUpdateDayKlines(klines_list_1d)) {
            	logger.info(pair + "交易对日线级别K线信息已更新");
            	klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
            }
            
            if(!klinesService.checkData(klines_list_1d)) {
            	klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
            }
            
            //查询15分钟级别k线信息
            List<Klines> klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);
            if(CollectionUtils.isEmpty(klines_list_15m)){
                logger.info("无法获取" + pair + "交易对15分钟级别K线信息");
                return;
            }
            
            if(!klinesService.checkData(klines_list_15m)) {
            	klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);
            }
            
            //查询1小时级别k线信息
            List<Klines> klines_list_1h_db = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 5000);
            List<Klines> klines_list_1h = PriceUtil.to1HFor15MKlines(klines_list_15m);
        	Klines last_1h = PriceUtil.getLastKlines(klines_list_1h);
            int minute = DateFormatUtil.getMinute(last_1h.getEndTime());
    		if(minute != 59) {
    			klines_list_1h.remove(last_1h);
    			last_1h = PriceUtil.getLastKlines(klines_list_1h);
    		}
            if(CollectionUtils.isEmpty(klines_list_1h_db)) {
            	if(!CollectionUtils.isEmpty(klines_list_1h)) {
                	klinesRepository.insert(klines_list_1h);
                	logger.info("已初始化{}交易对{}条1小时级别k线信息", pair, klines_list_1h.size());
            	}
            } else if(last_1h != null){
            	klinesRepository.insert(last_1h);
            }
            
            klines_list_1h_db = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 5000);
            if(!CollectionUtils.isEmpty(klines_list_1h_db)) {
                klinesService.checkData(klines_list_1h_db);
                klines_list_1h_db = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 5000);
            }
            
            //斐波那契回撤分析
            klinesService.futuresFibMonitor(klines_list_1h_db, klines_list_15m);
            
            //指数均线
            klinesService.futuresEmaRiseAndFallMonitor(klines_list_15m);
            
            //价格行为分析
            klinesService.futuresPriceAction(klines_list_15m);
            
            //盘整区分析
            klinesService.consolidationAreaMonitor(klines_list_1h_db, klines_list_15m);
            
            //量价分析
            klinesService.volumeMonitor(klines_list_15m);

        } catch (Exception e) {
            logger.error("分析" + pair + "交易对K线信息时出现异常", e);
        }
    }

}
