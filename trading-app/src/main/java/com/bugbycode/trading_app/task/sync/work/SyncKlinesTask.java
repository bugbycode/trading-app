package com.bugbycode.trading_app.task.sync.work;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
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

    private WorkTaskPool analysisWorkTaskPool;

    public SyncKlinesTask(String pair,Date now, KlinesService klinesService,
        KlinesRepository klinesRepository,WorkTaskPool analysisWorkTaskPool){
        this.pair = pair;
        this.now = now;
        this.klinesService = klinesService;
        this.klinesRepository = klinesRepository;
        this.analysisWorkTaskPool = analysisWorkTaskPool;
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
            //1小时级别
            List<Klines> klines_list_1h_db = klinesRepository.findByPair(pair, Inerval.INERVAL_1H.getDescption());

            klinesRepository.checkData(klines_list_1h_db);

            KlinesUtil ku_1h = new KlinesUtil(klines_list_1h_db);
            Klines lastKlines_1h = ku_1h.removeLast();
            

            //开盘时间
            long startTime = 0;

            Date endTime_1h = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
            Date startTime_1h = DateFormatUtil.getStartTimeBySetHour(endTime_1h, -Inerval.INERVAL_1H.getNumber() * 4 * 365);//limit根k线
            endTime_1h = DateFormatUtil.getStartTimeBySetMillisecond(endTime_1h, -1);//收盘时间

            if(lastKlines_1h != null){
                startTime = lastKlines_1h.getEndTime() + 1;
                if(klines_list_1h_db.size() > 3000){
                    Klines last_1h_klines = ku_1h.removeFirst();
                    klinesRepository.remove(last_1h_klines.getStartTime(), pair, Inerval.INERVAL_1H.getDescption());
                    logger.info(pair + "交易对1小时级别数据库中已超过3000条，将删除最旧的一条k线数据");
                }
            } else {
                startTime = startTime_1h.getTime();
            }

            //同步1小时级别k线信息
            List<Klines> klines_list_1h = klinesService.continuousKlines(pair, startTime, endTime_1h.getTime(), 
                        Inerval.INERVAL_1H.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
            if(!CollectionUtils.isEmpty(klines_list_1h)){
                
                logger.info("已获取到" + klines_list_1h.size() + "条" + pair + "交易对1小时级别k线信息");

                klinesRepository.insert(klines_list_1h);
            }

            //开始分析k线
            this.analysisWorkTaskPool.add(new AnalysisKlinesTask(pair,klinesService,klinesRepository));

        } catch (Exception e) {
            logger.error("同步" + pair + "交易对K线信息时出现异常", e);
			EmailUtil.send("程序运行出现异常", "同步" + pair + "交易对K线信息时出现异常，异常信息：" + e.getLocalizedMessage());
        }
    }
    
}
