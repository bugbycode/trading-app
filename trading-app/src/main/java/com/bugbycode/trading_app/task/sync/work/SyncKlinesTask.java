package com.bugbycode.trading_app.task.sync.work;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.util.DateFormatUtil;
import com.util.KlinesUtil;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

/**
 * 同步K线任务
 */
public class SyncKlinesTask implements Runnable{

    private final Logger logger = LogManager.getLogger(SyncKlinesTask.class);

    private String pair;

    private Date now;

    private KlinesService klinesService;

    private KlinesRepository klinesRepository;

    public SyncKlinesTask(String pair,Date now, KlinesService klinesService,
        KlinesRepository klinesRepository){
        this.pair = pair;
        this.now = now;
        this.klinesService = klinesService;
        this.klinesRepository = klinesRepository;
    }

    @Override
    public void run() {

        try {

            int hours = DateFormatUtil.getHours(now.getTime());
            Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
            Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
            Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, - 4 * 365);//多少天以前起始时间

            //日线级别
            //数据库中存储的k线信息
            List<Klines> klines_list_db = klinesRepository.findByPair(pair, Inerval.INERVAL_1D.getDescption());
            
            klinesRepository.checkData(klines_list_db);

            KlinesUtil ku = new KlinesUtil(klines_list_db);
            Klines lastDayKlines = ku.removeLast();
            
            if(lastDayKlines != null && lastDayKlines.getStartTime() >= lastDayStartTimeDate.getTime()){
                logger.info(pair + "交易对日线级别k线已是最新数据");
                return;
            } else {
                
                long startTime = lastDayKlines == null ? firstDayStartTime.getTime() : lastDayKlines.getEndTime() + 1;
                
                List<Klines> klines_list = klinesService.continuousKlines(pair, startTime, lastDayEndTimeDate.getTime(), 
                                            Inerval.INERVAL_1D.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
                
                if(!CollectionUtils.isEmpty(klines_list)){
                    
                    logger.info("已获取到" + klines_list.size() + "条" + pair + "交易对日线级别k线信息");
                    
                    klinesRepository.insert(klines_list);
                }
            }

            //=======================================================================================================
            //十五分钟级别
            List<Klines> klines_list_15m_db = klinesRepository.findByPair(pair, Inerval.INERVAL_15M.getDescption());

            klinesRepository.checkData(klines_list_15m_db);

            KlinesUtil ku_15m = new KlinesUtil(klines_list_15m_db);
            Klines lastKlines_15m = ku_15m.removeLast();
            //logger.info("数据库中已拥有" + pair + "15分钟级别" + klines_list_15m_db.size() + "条k线信息");

            //开盘时间
            long startTime = 0;

            Date endTime_15m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
            Date startTime_15m = DateFormatUtil.getStartTimeBySetMinute(endTime_15m, -Inerval.INERVAL_15M.getNumber() * 4 * 365);//limit根k线
            endTime_15m = DateFormatUtil.getStartTimeBySetMillisecond(endTime_15m, -1);//收盘时间

            if(lastKlines_15m != null){
                startTime = lastKlines_15m.getEndTime() + 1;
            } else {
                startTime = startTime_15m.getTime();
            }

            //同步15分钟级别k线信息
            List<Klines> klines_list_15m = klinesService.continuousKlines(pair, startTime, endTime_15m.getTime(), 
                        Inerval.INERVAL_15M.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
            if(!CollectionUtils.isEmpty(klines_list_15m)){
                
                logger.info("已获取到" + klines_list_15m.size() + "条" + pair + "交易对十五分钟级别k线信息");

                klinesRepository.insert(klines_list_15m);
            }

        } catch (Exception e) {
            logger.error("同步" + pair + "交易对K线信息时出现异常", e);
        }
    }
    
}