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

import java.util.Date;
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
            
            //查询15分钟级别k线信息 START ============================================================================
            List<Klines> klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);
            if(CollectionUtils.isEmpty(klines_list_15m)){
                logger.info("无法获取" + pair + "交易对15分钟级别K线信息");
                return;
            }
            
            if(!klinesService.checkData(klines_list_15m)) {
            	klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);
            }
            
            //查询15分钟级别k线信息 END ============================================================================
            
            //查询1小时级别k线信息 START ===============================================================================
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
            //查询1小时级别k线信息 END ===============================================================================
            
            //日线级别信息START====================================================================================
            //合并成昨日的日线级别
            int hours = DateFormatUtil.getHours(new Date().getTime());
            Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
            Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
            List<Klines> lastDay_15m = PriceUtil.subListForBetweenStartTimeAndEndTime(klines_list_15m, lastDayStartTimeDate.getTime(), lastDayEndTimeDate.getTime() + 1000);
            if(!CollectionUtils.isEmpty(lastDay_15m)) {
            	
            	//查询日线级别K线信息
                List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
                if(CollectionUtils.isEmpty(klines_list_1d)){
                    logger.info("无法获取" + pair + "交易对日线级别K线信息");
                    return;
                }
                
                Klines lastDay = PriceUtil.getLastKlines(klines_list_1d);
            	Klines parseKlines_1d = PriceUtil.parse(lastDay_15m, Inerval.INERVAL_1D);
            	//与最后一天不是同一根k线则直接入库
            	if(!lastDay.isEquals(parseKlines_1d)) {
            		klinesRepository.insert(parseKlines_1d);
                	
                	klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
                	//检查数据完整性
                	klinesService.checkData(klines_list_1d);
            	}
                
            }
            
            //日线级别信息END====================================================================================
            
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
