package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName file
 */
@Data
public class File implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 对应监测点的id
     */
    private Integer pointId;

    /**
     * 文件类型 0:图片 1:视频 2:政策文档pdf 3:其他
     */
    private String type;

    /**
     * 文件的后缀
     */
    private String postfix;

    /**
     * 文件名
     */
    private String name;

    /**
     * 时间
     */
    private Date time;

    private static final long serialVersionUID = 1L;

}