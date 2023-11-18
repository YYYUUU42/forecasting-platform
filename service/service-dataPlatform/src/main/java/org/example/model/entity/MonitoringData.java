package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName monitoring_data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringData implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 对应监测点的id
     */
    private Integer pointId;

    /**
     * 细胞密度
     */
    private String cellDensity;

    /**
     * 硝氮
     */
    private String nitrateNitrogen;

    /**
     * 亚硝氮
     */
    private String nitriteNitrogen;

    /**
     * 氨氮
     */
    private String ammoniaNitrogen;

    /**
     * 磷酸盐
     */
    private String phosphate;

    /**
     * 硅酸盐
     */
    private String silicate;

    /**
     * 叶绿素
     */
    private String chlorophyll;

    /**
     * 盐度
     */
    private String salinity;

    /**
     * 温度
     */
    private String temperature;

    /**
     * 溶解氧
     */
    private String dissolvedOxygen;

    /**
     * pH 值
     */
    private String ph;

    /**
     * 流速
     */
    private String flowRate;

    /**
     * 时间
     */
    private Date time;

    private static final long serialVersionUID = 1L;



}