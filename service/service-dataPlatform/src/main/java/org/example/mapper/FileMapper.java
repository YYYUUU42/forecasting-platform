package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.entity.File;

/**
* @author yu
* @description 针对表【file】的数据库操作Mapper
* @createDate 2023-08-13 20:27:41
* @Entity org.example.model.entity.File
*/
@Mapper
public interface FileMapper extends BaseMapper<File> {
}




