package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.model.entity.WeatherData;
import org.example.model.vo.WeatherVO;

import java.util.List;

/**
 * @author sunnyherry
 * @description 针对表【weather_data】的数据库操作Mapper
 * @createDate 2023-10-08 21:46:33
 * @Entity generator.domain.weatherData
 */
public interface WeatherDataMapper extends BaseMapper<WeatherData> {

    /**
     * 查询气象图图片数据
     *
     * @param weatherData
     * @return
     */
    List<WeatherVO> searchData(WeatherData weatherData);
}
