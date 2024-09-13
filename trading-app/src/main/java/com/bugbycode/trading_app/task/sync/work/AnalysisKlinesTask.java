package com.bugbycode.trading_app.task.sync.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.List;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.repository.KlinesRepository;
import com.bugbycode.service.KlinesService;
import com.util.EmailUtil;

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
        logger.info("开始执行k线分析任务");
        try {
            //查询日线级别K线信息
            List<Klines> klines_list_1d = klinesRepository.findByPair(pair, Inerval.INERVAL_1D.getDescption());
            if(CollectionUtils.isEmpty(klines_list_1d)){
                logger.info("无法获取" + pair + "日线级别K线信息");
                return;
            }
            //查询15分钟级别k线信息
            List<Klines> klines_list_15m = klinesRepository.findByPair(pair, Inerval.INERVAL_15M.getDescption());
            if(CollectionUtils.isEmpty(klines_list_15m)){
                logger.info("无法获取" + pair + "15分钟级别K线信息");
                return;
            }

            //斐波那契回撤分析
            klinesService.futuresFibMonitor(klines_list_1d, klines_list_15m);

            //标志性高低点分析
            klinesService.futuresHighOrLowMonitor(klines_list_1d, klines_list_15m);

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
        }
        logger.info("k线分析任务执行完成");
    }

}
