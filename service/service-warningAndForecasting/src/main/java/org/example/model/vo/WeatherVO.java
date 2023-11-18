package org.example.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeatherVO implements Serializable {
    private String vapor;
    private String precipitation;
    private String dbz;
    private String hgtWind;
    private String huguangStation;
    private String liushaPortStation;
    private String liushaPortProfile;
    private String huguangProfile;
    private String yangjiangStation;
    private String yangjiangProfile;
    private String time;
}
