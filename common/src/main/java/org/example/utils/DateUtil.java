package org.example.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    /**
     * 计算天数差
     *
     * @param start
     * @param end
     */
    public static Long endDatereturn(String start, String end) {
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date star = null;//开始时间
        Date endDay = null;//结束时间
        try {
            star = dft.parse(start);
            endDay = dft.parse(end);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Long starTime = star.getTime();
        Long endTime = endDay.getTime();
        Long num = endTime - starTime;//时间戳相差的毫秒数
        long days = num / 24 / 60 / 60 / 1000;
        return days;

    }

}
