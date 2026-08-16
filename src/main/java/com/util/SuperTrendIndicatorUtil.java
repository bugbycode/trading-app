package com.util;

import java.util.List;

import com.bugbycode.module.Klines;

public class SuperTrendIndicatorUtil {

	/**
     * SuperTrend计算
     *
     * @param klines      K线数据
     */
    public static void calculate(List<Klines> klines) {
    	calculate(klines, 14, 3);
    }
	
    /**
     * SuperTrend计算
     *
     * @param klines      K线数据
     * @param atrPeriod   ATR周期
     * @param multiplier  ATR倍数
     */
    public static void calculate(
            List<Klines> klines,
            int atrPeriod,
            double multiplier) {

        double atr = 0;

        double prevFinalUpper = 0;
        double prevFinalLower = 0;
        double prevSuperTrend = 0;


        for (int i = 0; i < klines.size(); i++) {

            Klines current = klines.get(i);

            if (i < atrPeriod) {
                current.setSuperTrend(0);
                current.setTrend(true);
                continue;
            }


            // Wilder RMA ATR
            if (i == atrPeriod) {

                atr = initATR(
                        klines,
                        i,
                        atrPeriod
                );

            } else {

                double tr = trueRange(
                        klines,
                        i
                );

                atr = (
                        atr * (atrPeriod - 1)
                        + tr
                ) / atrPeriod;
            }


            double high = Double.parseDouble(
                    current.getHighPrice()
            );

            double low = Double.parseDouble(
                    current.getLowPrice()
            );

            double close = Double.parseDouble(
                    current.getClosePrice()
            );


            double hl2 = (high + low) / 2;


            double basicUpper =
                    hl2 + multiplier * atr;


            double basicLower =
                    hl2 - multiplier * atr;


            double finalUpper;

            double finalLower;


            // 上轨递推
            if (basicUpper < prevFinalUpper
                    || close(klines, i - 1) > prevFinalUpper) {

                finalUpper = basicUpper;

            } else {

                finalUpper = prevFinalUpper;
            }


            // 下轨递推
            if (basicLower > prevFinalLower
                    || close(klines, i - 1) < prevFinalLower) {

                finalLower = basicLower;

            } else {

                finalLower = prevFinalLower;
            }


            boolean trend;
            double superTrend;


            /*
             * 趋势判断
             */
            if (prevSuperTrend == prevFinalUpper) {

                if (close > finalUpper) {

                    trend = true;
                    superTrend = finalLower;

                } else {

                    trend = false;
                    superTrend = finalUpper;
                }

            } else {

                if (close < finalLower) {

                    trend = false;
                    superTrend = finalUpper;

                } else {

                    trend = true;
                    superTrend = finalLower;
                }
            }


            current.setSuperTrend(
                    round(superTrend)
            );

            current.setTrend(trend);


            prevFinalUpper = finalUpper;
            prevFinalLower = finalLower;
            prevSuperTrend = superTrend;
        }
    }


    /**
     * 初始化ATR
     * 第一周期使用SMA
     */
    private static double initATR(
            List<Klines> klines,
            int index,
            int period) {

        double sum = 0;

        for (int i = index - period + 1; i <= index; i++) {

            sum += trueRange(
                    klines,
                    i
            );
        }

        return sum / period;
    }


    /**
     * True Range
     */
    private static double trueRange(
            List<Klines> klines,
            int index) {

        double high = Double.parseDouble(
                klines.get(index).getHighPrice()
        );

        double low = Double.parseDouble(
                klines.get(index).getLowPrice()
        );

        double prevClose = Double.parseDouble(
                klines.get(index - 1).getClosePrice()
        );


        return Math.max(
                high - low,
                Math.max(
                        Math.abs(high - prevClose),
                        Math.abs(low - prevClose)
                )
        );
    }


    private static double close(
            List<Klines> klines,
            int index) {

        return Double.parseDouble(
                klines.get(index).getClosePrice()
        );
    }


    private static double round(
            double value) {

        return Math.round(
                value * 100000d
        ) / 100000d;
    }
}