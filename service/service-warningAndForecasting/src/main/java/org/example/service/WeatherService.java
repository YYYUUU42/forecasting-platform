package org.example.service;

import org.example.model.vo.WeatherVO;

import java.util.Map;

public interface WeatherService {
    /**
     * 自动化下载、上传和写入气象图数据
     */
    void downloadData();

    /**
     * 按指定日期编号查询最近七天特定日期的气象图数据
     */
    Map<String,WeatherVO> searchWeatherData(String dateNumber);
}
