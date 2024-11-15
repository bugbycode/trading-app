package com.bugbycode.trading_app.task.sync.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.List;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;

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
            List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(),5000);
            if(CollectionUtils.isEmpty(klines_list_1d)){
                logger.info("无法获取" + pair + "交易对日线级别K线信息");
                return;
            }
            
            if(!klinesRepository.checkData(klines_list_1d)) {
            	klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(),5000);
            }
            
            //查询15分钟级别k线信息
            List<Klines> klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(),5000);
            if(CollectionUtils.isEmpty(klines_list_15m)){
                logger.info("无法获取" + pair + "交易对15分钟级别K线信息");
                return;
            }
            
            if(!klinesRepository.checkData(klines_list_15m)) {
            	klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(),5000);
            }
            
            //斐波那契回撤分析
            klinesService.futuresFibMonitor(klines_list_1d, klines_list_15m);
            
            //涨跌分析
            klinesService.futuresRiseAndFall(klines_list_15m);

            //EMA指标分析
            klinesService.futuresEMAMonitor(klines_list_15m);

            //EMA指标涨跌判断
            klinesService.futuresEmaRiseAndFall(klines_list_15m);

            //标志性高低点分析
            klinesService.futuresHighOrLowMonitor(klines_list_1d, klines_list_15m);

        } catch (Exception e) {
            logger.error("分析" + pair + "交易对K线信息时出现异常", e);
        }
    }

}
