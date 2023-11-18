package org.example.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.exception.CustomException;
import org.example.mapper.MonitoringDataMapper;
import org.example.mapper.PointsMapper;
import org.example.model.entity.MonitoringData;
import org.example.model.enums.AppHttpCodeEnum;
import org.example.model.vo.MonitoringDataMapVo;
import org.example.service.FrontPageService;
import org.example.utils.ResponseResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FrontPageServiceImpl implements FrontPageService {

    @Resource
    private PointsMapper pointsMapper;

    @Resource
    private MonitoringDataMapper monitoringDataMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 获取最近五天的日报数据
     *
     * @param type      监测数据的类型
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    @Override
    public ResponseResult getDailyPaperByMonitoringType(String type, String latitude, String longitude) {
        if (!StringUtils.hasText(type) || latitude == null || longitude == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //获得pointId
        Integer pointId = pointsMapper.getPointId(latitude, longitude);

        //得到该坐标的监测数据
        List<MonitoringData> monitoringDataList = getMonitoringDataList(pointId, "day");

        //创建日期格式化对象
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        //先对数据进行整理,只剩下需要选中的监测数据的类型和时间
        //collect部分就是对数据再次进行整理，算出平均值,利用TreeMap的有序性对时间进行排序
        TreeMap<String, Double> res = monitoringDataList.stream()
                .map(monitoringData -> {
                    MonitoringDataMapVo mapVo = new MonitoringDataMapVo();
                    String time = format.format(monitoringData.getTime());
                    Double data = getFieldValueByName(type, monitoringData);

                    mapVo.setTime(time);
                    mapVo.setData(data);
                    return mapVo;
                }).collect(Collectors.groupingBy(
                        MonitoringDataMapVo::getTime,
                        TreeMap::new,
                        Collectors.averagingDouble(MonitoringDataMapVo::getData)
                ));


        //保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        res.forEach((time, data) -> {
            Double formattedValue = Double.valueOf(decimalFormat.format(data));
            res.put(time, formattedValue);
        });

        //对时间进行补全，就五天时间内没有数据的时间段，数据填充为0.0
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            LocalDate date = today.minusDays(-i);
            String dateString = date.toString();
            res.putIfAbsent(dateString, 0.00);
        }


        return ResponseResult.okResult(res);
    }




    /**
     * 根据属性名获取属性值
     *
     * @param fieldName
     * @param monitoringData
     * @return
     */
    private Double getFieldValueByName(String fieldName, MonitoringData monitoringData) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = monitoringData.getClass().getMethod(getter, new Class[]{});
            Double value = Double.valueOf(method.invoke(monitoringData, new MonitoringData[]{}).toString());
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 封装得到MonitoringDataList
     *
     * @param pointId
     * @param type
     * @return
     */
    public List<MonitoringData> getMonitoringDataList(Integer pointId, String type) {
        // 构造起始日期和现在时间
        Calendar calendar = Calendar.getInstance();

        Date startDate = null;
        Date endDate = null;

        if (type.equals("hour")) {
            //包含现在的时间
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            startDate = calendar.getTime();

            //12小时后的时间
            calendar.add(Calendar.HOUR_OF_DAY, 12);
            endDate = calendar.getTime();
        } else if (type.equals("day")) {
            try {
                calendar.add(Calendar.DAY_OF_MONTH, 4);
                endDate = calendar.getTime();

                //获得当天凌晨十二点的时间
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String format = dateFormat.format(new Date(System.currentTimeMillis()));
                startDate = dateFormat.parse(format);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equals("month")) {
            // 构造起始日期和现在时间
            calendar = Calendar.getInstance();

            //得到十二个月前的时间
            calendar.add(Calendar.MONTH, -12);
            startDate = calendar.getTime();
            endDate = new Date(System.currentTimeMillis());

        }

        //得到该坐标的所有数据
        LambdaQueryWrapper<MonitoringData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitoringData::getPointId, pointId)
                .ge(MonitoringData::getTime, startDate)
                .le(MonitoringData::getTime, endDate);
        List<MonitoringData> monitoringDataList = monitoringDataMapper.selectList(queryWrapper);

        return monitoringDataList;
    }

    /**
     * 显示12小时的信息
     *
     * @param latitude
     * @param longitude
     * @return
     */
    @Override
    public ResponseResult getHourPaper(String type, String latitude, String longitude) {
        if (!StringUtils.hasText(type) || latitude == null || longitude == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //获得pointId
        Integer pointId = pointsMapper.getPointId(latitude, longitude);

        //得到该坐标的监测数据
        List<MonitoringData> monitoringDataList = getMonitoringDataList(pointId, "hour");

        //创建日期格式化对象
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        Map<String, Double> res = monitoringDataList.stream().map(monitoringData -> {
            MonitoringDataMapVo mapVo = new MonitoringDataMapVo();
            String time = format.format(monitoringData.getTime());
            //得到数值
            Double data = getFieldValueByName(type, monitoringData);
            //小数点保留两位
            Double value = Double.valueOf(decimalFormat.format(data));

            mapVo.setTime(time);
            mapVo.setData(value);
            return mapVo;
        }).collect(Collectors.toMap(k -> k.getTime(), v -> v.getData(), (k1, k2) -> k1, TreeMap::new));

        //对时间进行补全，12小时没有数据的时间段，数据填充为0.00
        LocalDateTime currentDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 12; i++) {
            LocalDateTime dateTime = currentDateTime.minusHours(-i);
            String dateTimeString = dateTime.format(formatter);
            res.putIfAbsent(dateTimeString, 0.00);
        }

        return ResponseResult.okResult(res);
    }

    /**
     * 显示之前十二个月的信息
     *
     * @param type
     * @param latitude
     * @param longitude
     * @return
     */
    @Override
    public ResponseResult getMonthPaper(String type, String latitude, String longitude) {
        String key=type+latitude+longitude;
        if (stringRedisTemplate.hasKey(key)){
            String s = stringRedisTemplate.opsForValue().get(key);
            TreeMap json = JSONObject.parseObject(s, TreeMap.class);
            return ResponseResult.okResult(json);
        }

        if (!StringUtils.hasText(type) || latitude == null || longitude == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //获得pointId
        Integer pointId = pointsMapper.getPointId(latitude, longitude);

        //创建日期格式化对象
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");

        //得到该坐标的所有数据
        List<MonitoringData> monitoringDataList = getMonitoringDataList(pointId, "month");

        //先对数据进行整理,只剩下需要选中的监测数据的类型和时间
        //collect部分就是对数据再次进行整理，算出平均值,利用TreeMap的有序性对时间进行排序
        TreeMap<String, Double> res = monitoringDataList.stream()
                .map(monitoringData -> {
                    MonitoringDataMapVo mapVo = new MonitoringDataMapVo();
                    String time = format.format(monitoringData.getTime());
                    Double data = getFieldValueByName(type, monitoringData);

                    mapVo.setTime(time);
                    mapVo.setData(data);
                    return mapVo;
                }).collect(Collectors.groupingBy(
                        MonitoringDataMapVo::getTime,
                        TreeMap::new,
                        Collectors.averagingDouble(MonitoringDataMapVo::getData)
                ));


        //保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        res.forEach((time, data) -> {
            Double formattedValue = Double.valueOf(decimalFormat.format(data));
            res.put(time, formattedValue);
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        //对时间进行补全，就十二个时间内没有数据的时间段，数据填充为0.0
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate date = today.minusMonths(i);
            String dateString = date.format(formatter);
            res.putIfAbsent(dateString, 0.00);
        }

        String s = JSONObject.toJSONString(res);
        stringRedisTemplate.opsForValue().set(key,s);

        return ResponseResult.okResult(res);
    }


}
