package org.example.controller;

import org.example.service.FrontPageService;
import org.example.utils.ResponseResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/service/serviceDatePlatform/FrontPage")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FrontPageController {

    @Resource
    private FrontPageService frontPageService;

    /**
     * 获取最近五天的日报数据
     * @param type 监测数据的类型
     * @param latitude 纬度
     * @param longitude 经度
     * @return
     */
    @GetMapping("/getDailyPaper")
    public ResponseResult getDailyPaperByMonitoringType(String type,String latitude,String longitude){
        return frontPageService.getDailyPaperByMonitoringType(type,latitude,longitude);
    }

    /**
     * 显示当天12小时的信息
     * @param type
     * @param latitude
     * @param longitude
     * @return
     */
    @GetMapping("/getHourPaper")
    public ResponseResult getHourPaper(String type,String latitude,String longitude){
        return frontPageService.getHourPaper(type,latitude,longitude);
    }

    /**
     * 显示之前十二个月的信息
     * @param type
     * @param latitude
     * @param longitude
     * @return
     */
    @GetMapping("/getMonthPaper")
    public ResponseResult getMonthPaper(String type,String latitude,String longitude){
        return frontPageService.getMonthPaper(type,latitude,longitude);
    }

}
