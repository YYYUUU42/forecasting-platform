package org.example.controller;

import org.example.service.WeatherService;
import org.example.utils.ResponseResult;
import org.example.model.vo.WeatherVO;
import org.example.utils.WeatherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/service/serviceWarningAndForecasting/Weather")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WeatherController {

    @Resource
    private WeatherService weatherService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 自动化下载气象图
     *
     * @return
     * @throws IOException
     */
    @GetMapping("/autoDownload")
    public ResponseResult AutoDownloadData() throws Exception {
        weatherService.downloadData();
        return ResponseResult.okResult();
    }

    /**
     * 按指定日期编号查询最近七天特定日期的气象图数据
     *
     * @return
     */
    @GetMapping("/searchOneDayWeatherData")
    public ResponseResult searchWeatherData(String dateNumber) {
        //获取当前时间并设置最新文件日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String newestFileDate = LocalDateTime.now().minusDays(1).format(dateTimeFormatter);

        //查找redis中是否有当前指定日期的缓存数据
        String dataTimeKey = WeatherUtil.resolveDate(dateNumber, newestFileDate).toString();
        //设置redis的key的分类规则
        String key = "weatherData_" + dataTimeKey;
        Map<String, WeatherVO> weatherData = (Map<String, WeatherVO>) redisTemplate.opsForValue().get(key);
        if (weatherData != null) {
            //删除不在一周内的缓存数据
            String dateTime = LocalDateTime.now().minusDays(8).format(dateTimeFormatter);
            String oldDataKey = "weatherData_" + dateTime;
            clenCache(oldDataKey);
            //缓存存在则直接从redis中获取并返回缓存数据
            return ResponseResult.okResult(weatherData);
        }
        //若缓存不为空，则到数据库查找并缓存数据
        Map<String, WeatherVO> resultWeatherVOMap = weatherService.searchWeatherData(dateNumber);
        redisTemplate.opsForValue().set(key, resultWeatherVOMap);


        return ResponseResult.okResult(resultWeatherVOMap);
    }

    /**
     * 清除缓存
     * @param pattern
     */
    private void clenCache(String pattern) {
        Set key = redisTemplate.keys(pattern);
        redisTemplate.delete(key);
    }


}
