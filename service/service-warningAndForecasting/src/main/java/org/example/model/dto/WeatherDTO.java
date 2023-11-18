package org.example.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeatherDTO implements Serializable {

    private String vapor;
    private String precipitation;
    private String dbz;
    private String hgtWind;
    private String liushaPortStation;
    private String huguangStation;
    private String liushaPortProfile;
    private String huguangProfile;
    private String yangjiangStation;
    private String yangjiangProfile;
    private String time;

}
