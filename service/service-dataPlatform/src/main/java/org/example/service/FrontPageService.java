package org.example.service;


import org.example.utils.ResponseResult;

public interface FrontPageService {

    /**
     * 获取最近五天的日报数据
     *
     * @param type      监测数据的类型
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    ResponseResult getDailyPaperByMonitoringType(String type, String latitude, String longitude);

    /**
     *显示12小时的信息
     * @param latitude
     * @param longitude
     * @return
     */
    ResponseResult getHourPaper(String type,String latitude, String longitude);


    /**
     *显示之前十二个月的信息
     * @param latitude
     * @param longitude
     * @return
     */
    ResponseResult getMonthPaper(String type, String latitude, String longitude);
}
