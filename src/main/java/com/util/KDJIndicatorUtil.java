package com.util;

import java.util.List;

import com.bugbycode.module.Klines;


public class KDJIndicatorUtil {

	/**
     * KDJ指标计算
     *
     * @param klines K线数据
     */
	public static void calculate(List<Klines> klines) {
		calculate(klines, 9);
	}

    /**
     * KDJ指标计算
     *
     * @param klines K线数据
     * @param n RSV周期 默认9
     */
    public static void calculate(List<Klines> klines, int n) {


        double k = 50;
        double d = 50;


        for (int i = 0; i < klines.size(); i++) {


            // 前n-1根无法计算
            if (i < n - 1) {

                klines.get(i).setK(0);
                klines.get(i).setD(0);
                klines.get(i).setJ(0);

                continue;
            }


            double high = getHigh(
                    klines,
                    i - n + 1,
                    i
            );


            double low = getLow(
                    klines,
                    i - n + 1,
                    i
            );


            double close =
                    Double.parseDouble(
                            klines.get(i).getClosePrice()
                    );


            double rsv;


            if (high == low) {

                rsv = 0;

            } else {

                rsv =
                (close - low)
                /
                (high - low)
                *
                100;
            }



            /*
             * K = 2/3 * K(previous)
             *     + 1/3 * RSV
             */
            k =
            (2.0 / 3.0) * k
            +
            (1.0 / 3.0) * rsv;



            /*
             * D = 2/3 * D(previous)
             *     + 1/3 * K
             */
            d =
            (2.0 / 3.0) * d
            +
            (1.0 / 3.0) * k;



            /*
             * J = 3K - 2D
             */
            double j =
                    3 * k - 2 * d;



            Klines line = klines.get(i);

            line.setK(round(k));
            line.setD(round(d));
            line.setJ(round(j));

        }

    }



    private static double getHigh(
            List<Klines> list,
            int start,
            int end
    ){

        double high =
                Double.parseDouble(
                        list.get(start).getHighPrice()
                );


        for(int i=start+1;i<=end;i++){

            double value =
                    Double.parseDouble(
                        list.get(i).getHighPrice()
                    );


            if(value > high){
                high=value;
            }
        }


        return high;
    }



    private static double getLow(
            List<Klines> list,
            int start,
            int end
    ){

        double low =
                Double.parseDouble(
                        list.get(start).getLowPrice()
                );


        for(int i=start+1;i<=end;i++){

            double value =
                    Double.parseDouble(
                        list.get(i).getLowPrice()
                    );


            if(value < low){
                low=value;
            }
        }


        return low;
    }



    private static double round(double value){

        return Math.round(value * 100.0) / 100.0;

    }

}