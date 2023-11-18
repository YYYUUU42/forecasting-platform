package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 水汽通量图
     */
    private String vapor;

    /**
     * 三小时累计降水图
     */
    private String precipitation;

    /**
     * 雷达回波图
     */
    private String dbz;

    /**
     * 500hPa高度的风场和高度场图
     */
    private String hgtWind;

    /**
     * 流沙湾廓线预报监测
     */
    private String liushaPortStation;

    /**
     * 流沙湾湿度-温度-风速预报监测
     */
    private String liushaPortProfile;

    /**
     * 湖光校区廓线预报监测
     */
    private String huguangStation;

    /**
     * 湖光校区湿度-温度-风速预报监测
     */
    private String huguangProfile;

    /**
     * 阳江校区廓线预报监测
     */
    private String yangjiangStation;

    /**
     * 阳江校区湿度-温度-风速预报监测
     */
    private String yangjiangProfile;

    /**
     * 图预报时间
     */
    private Date time;
}
