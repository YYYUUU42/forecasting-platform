package org.example.utils;

import org.example.mapper.MonitoringDataMapper;
import org.example.model.entity.MonitoringData;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 创造假数据
 */
public class GetFalseData {

    @Resource
    private MonitoringDataMapper monitoringDataMapper;

    public  void generateAndInsertFakeData() {
        Random random = new Random();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        for (int i = 101; i <= 300; i++) {
            MonitoringData data = new MonitoringData();
            data.setPointId(1);
            data.setCellDensity(decimalFormat.format(random.nextDouble() * 150));
            data.setNitrateNitrogen(decimalFormat.format(random.nextDouble() * 20));
            data.setNitriteNitrogen(decimalFormat.format(random.nextDouble() * 5));
            data.setAmmoniaNitrogen(decimalFormat.format(random.nextDouble() * 10));
            data.setPhosphate(decimalFormat.format(random.nextDouble() * 5));
            data.setSilicate(decimalFormat.format(random.nextDouble() * 30));
            data.setChlorophyll(decimalFormat.format(random.nextDouble() * 50));
            data.setSalinity(decimalFormat.format(random.nextDouble() * 35));
            data.setDissolvedOxygen(decimalFormat.format(random.nextDouble() * 12));
            data.setPh(decimalFormat.format(random.nextDouble() * 14));
            data.setFlowRate(decimalFormat.format(random.nextDouble() * 2));
            data.setTemperature(decimalFormat.format(random.nextDouble() * 30));
            try {
                String string = "2023-08-13 12:00:00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date parse = sdf.parse(string);
                data.setTime(parse);
            }catch (Exception e){
                e.printStackTrace();
            }

            monitoringDataMapper.insert(data);
        }

    }

    public void decreaseHour(){
        List<MonitoringData> list = monitoringDataMapper.selectList(null);
        for (MonitoringData monitoringData : list) {
            Integer id = monitoringData.getId();
            monitoringData.setTime(addHour(monitoringData.getTime(),-id));
            monitoringDataMapper.updateById(monitoringData);
        }
    }

    public  Date addHour(Date date,int i){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, i);
        Date newDate = c.getTime();
        return newDate;
    }
}
