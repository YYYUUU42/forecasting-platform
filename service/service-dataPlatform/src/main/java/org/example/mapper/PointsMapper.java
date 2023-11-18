package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.model.entity.Points;


/**
* @author yu
* @description 针对表【points】的数据库操作Mapper
* @createDate 2023-08-14 10:34:57
* @Entity generator.domain.Points
*/
public interface PointsMapper extends BaseMapper<Points> {

    /**
     * 感觉给出的纬度和经度，给出所在点的PointId
     * @param latitude
     * @param longitude
     * @return
     */
    Integer getPointId(String latitude,String longitude);

}




