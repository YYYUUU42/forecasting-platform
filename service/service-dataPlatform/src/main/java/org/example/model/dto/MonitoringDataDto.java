package org.example.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringDataDto {

    /**
     * 细胞密度
     */
    @ExcelProperty(value = "细胞密度" ,index = 0)
    private String cellDensity;

    /**
     * 硝氮
     */
    @ExcelProperty(value = "硝氮" ,index = 1)
    private String nitrateNitrogen;

    /**
     * 亚硝氮
     */
    @ExcelProperty(value = "亚硝氮" ,index = 1)
    private String nitriteNitrogen;

    /**
     * 氨氮
     */
    @ExcelProperty(value = "氨氮" ,index = 3)
    private String ammoniaNitrogen;

    /**
     * 磷酸盐
     */
    @ExcelProperty(value = "磷酸盐" ,index = 4)
    private String phosphate;

    /**
     * 硅酸盐
     */
    @ExcelProperty(value = "硅酸盐" ,index = 5)
    private String silicate;

    /**
     * 叶绿素
     */
    @ExcelProperty(value = "叶绿素" ,index = 6)
    private String chlorophyll;

    /**
     * 盐度
     */
    @ExcelProperty(value = "盐度" ,index = 7)
    private String salinity;

    /**
     * 温度
     */
    @ExcelProperty(value = "温度" ,index = 8)
    private String temperature;

    /**
     * 溶解氧
     */
    @ExcelProperty(value = "溶解氧" ,index = 9)
    private String dissolvedOxygen;

    /**
     * pH
     */
    @ExcelProperty(value = "pH" ,index = 10)
    private String ph;

    /**
     * 流速
     */
    @ExcelProperty(value = "流速" ,index = 11)
    private String flowRate;

    /**
     * 时间
     */
    @ExcelProperty(value = "时间" ,index = 12)
    private String time;
}
