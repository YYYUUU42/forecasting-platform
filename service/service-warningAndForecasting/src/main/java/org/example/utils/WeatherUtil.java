package org.example.utils;

import com.file.service.FileStorageService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.example.exception.CustomException;
import org.example.mapper.WeatherDataMapper;
import org.example.model.entity.WeatherData;
import org.example.model.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class WeatherUtil {
    public static WeatherUtil weatherUtil;
    @Resource
    private WeatherDataMapper weatherDataMapper;
    @Resource
    private FileStorageService fileStorageService;

    //初始化FileStorageService,否则注入失败，报空指针异常
    @PostConstruct
    public void initService() {
        weatherUtil = this;
        weatherUtil.fileStorageService = this.fileStorageService;
    }

    //初始化WeatherDataMapper，否则注入失败，报空指针异常
    @PostConstruct
    public void initMapper() {
        weatherUtil = this;
        weatherUtil.weatherDataMapper = this.weatherDataMapper;
    }


    /**
     * 将文件夹的图片上传到minio并返回外链写入到数据库
     *
     * @param fileName
     * @param folderPath
     */
    public static void inputDatabase(String fileName, String folderPath) {
        // 要查找的文件名列表,通过filename进行字符串匹配
        String newFile = fileName.substring(0, 4) + "-" + fileName.substring(4, 6) + "-" + fileName.substring(6, 8);

        //想法是通过文件名数组得到MultipartFiles集合，就可以用uploadFile得到外链了
        String[] fileNames = {
                folderPath + "\\vapor_flux\\vapor_flux.gif",
                folderPath + "\\precipitation\\precipitation.gif",
                folderPath + "\\dbz\\dbz.gif",
                folderPath + "\\500_hgt_wind\\500_hgt_wind.gif",
                folderPath + "\\Zhanjiang_liusha_port_station_" + newFile + ".png",
                folderPath + "\\Zhanjiang_liusha_port_profile.png",
                folderPath + "\\station_" + newFile + ".png",
                folderPath + "\\profile.png",
                folderPath + "\\yangjiang_station_" + newFile + ".png",
                folderPath + "\\yangjiang_profile.png"
        };

        //这里就得到uploadFile
        List<MultipartFile> multipartFiles = WeatherUtil.getMultipartFile(fileNames);

        String filePath = fileName.substring(0, 4) + "/" + fileName.substring(4, 6) + "/" + fileName.substring(6, 8);

        //创建weatherData对象
        WeatherData weatherData = new WeatherData();

        if (multipartFiles.isEmpty()) {
            //若数组集合为空抛出异常
            throw new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //String originalFilename = multipartFile.getOriginalFilename();
        //String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //调用minio获取外链
        //师兄的代码使用了uuid随机数作为文件名，因为这里的图片文件名全是固定的，那么只要前缀是日期就可以了，剩下的可以每天的都一样我就写死了图片名
        weatherData.setVapor(weatherUtil.fileStorageService.uploadFile("", filePath + "/vapor_flux.gif", multipartFiles.get(0)));
        weatherData.setPrecipitation(weatherUtil.fileStorageService.uploadFile("", filePath + "/precipitation.gif", multipartFiles.get(1)));
        weatherData.setDbz(weatherUtil.fileStorageService.uploadFile("", filePath + "/dbz.gif", multipartFiles.get(2)));
        weatherData.setHgtWind(weatherUtil.fileStorageService.uploadFile("", filePath + "/500_hgt_wind.gif", multipartFiles.get(3)));
        weatherData.setLiushaPortStation(weatherUtil.fileStorageService.uploadFile("", filePath + "/liusha_port_station.png", multipartFiles.get(4)));
        weatherData.setLiushaPortProfile(weatherUtil.fileStorageService.uploadFile("", filePath + "/liusha_port_profile.png", multipartFiles.get(5)));
        weatherData.setHuguangStation(weatherUtil.fileStorageService.uploadFile("", filePath + "/huguang_station.png", multipartFiles.get(6)));
        weatherData.setHuguangProfile(weatherUtil.fileStorageService.uploadFile("", filePath + "/huguang_profile.png", multipartFiles.get(7)));
        weatherData.setYangjiangStation(weatherUtil.fileStorageService.uploadFile("", filePath + "/yangjiang_station.png", multipartFiles.get(8)));
        weatherData.setYangjiangProfile(weatherUtil.fileStorageService.uploadFile("", filePath + "/yangjiang_profile.png", multipartFiles.get(9)));

        //时间对应文件夹的日期对应而不是添加入数据库的时间，防止出错
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //解析输入日期字符串
            Date date = inputFormat.parse(filePath);

            // 设置时分秒的默认值，这个默认值可以更改
            String formattedDateStr = outputFormat.format(date) + " 00:00:00";

            // 将格式化后的字符串转换回Date对象
            Date formattedDate = outputFormat.parse(formattedDateStr);
            //存入对象中
            weatherData.setTime(formattedDate);
        } catch (ParseException e) {
            throw new CustomException(AppHttpCodeEnum.DATE_ERROR);
        }
        //入库
        weatherUtil.weatherDataMapper.insert(weatherData);
    }

    /**
     * 根据文件名得到MultipartFile对象数组
     *
     * @param fileNames 文件名数组
     * @return MultipartFile对象数组
     */
    public static List<MultipartFile> getMultipartFile(String[] fileNames) {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            //根据文件名找文件创建File对象
            File file = new File(fileName);
            if (file.exists()) {
                //因为下载下来的路径是固定的，所以不需要递归查找文件夹，后续如果有变更可以加上
                /*if(file.isDirectory()) {
                    //如果是目录，递归查找并将所有文件加入结果列表
                    multipartFiles.addAll(getMultipartFileFromDirectory(file));
                } else {*/
                //如果是文件，将其转化成MultipartDile 对象并加入结果列表
                try {
                    DiskFileItem fileItem = null;
                    if (i < 4) {//这个判断可以 .gif字符串调用equals方法，我按照顺序那么这样最简单
                        /*
                        DiskFileItem 类是 Apache Commons FileUpload 库中的一个类，用于表示一个文件项（File Item）。
                        当处理文件上传时，用户提交的文件会被封装成 DiskFileItem 对象，以便在服务器端进行处理。

                        isFormField（是否是表单字段）: 指示这个文件项是否是一个普通的表单字段（例如文本输入框），还是一个文件字段。
                        如果是普通字段，值为 true，如果是文件字段，值为 false。

                        第三个参数影响着第一个参数是否有意义，所以这里的fileName无实际意义
                         */
                        fileItem = new DiskFileItem(fileName, "image/gif", false, file.getName(), (int) file.length(), file.getParentFile());
                    } else {
                        fileItem = new DiskFileItem(fileName, "image/png", false, file.getName(), (int) file.length(), file.getParentFile());
                    }
                    //文件的数据的缓冲输入流，用于读取文件的数据
                    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                        byte[] fileData = new byte[(int) file.length()];
                        //从输入流中读取文件的数据，并将其存储到fileData字节数组中。
                        inputStream.read(fileData);
                        //将文件流写入到fileItem中存储起来
                        fileItem.getOutputStream().write(fileData);
                        //将DiskFileItem对象转换为Spring框架中的MultipartFile接口的实现类
                        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                        multipartFiles.add(multipartFile);
                    }
                } catch (IOException e) {
                    throw new CustomException(AppHttpCodeEnum.PICTURE_IO_FILE_ERROR);
                }
                //}
            }
        }
        return multipartFiles;
    }

    /**
     * 递归文件夹查找
     *
     * @param directory 文件夹
     * @return MultipartFile对象数组
     */
    public List<MultipartFile> getMultipartFileFromDirectory(File directory) {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归查找子目录
                    multipartFiles.addAll(getMultipartFileFromDirectory(file));
                } else {
                    // 将文件转换为 MultipartFile 对象
                    try {
                        MultipartFile multipartFile = new CommonsMultipartFile((FileItem) file.toURI().toURL().openStream());
                        multipartFiles.add(multipartFile);
                    } catch (IOException e) {
                        throw new CustomException(AppHttpCodeEnum.UNZIP_ERROR);
                    }
                }
            }
        }
        return multipartFiles;
    }

    /**
     * 纠正错误的解压地址（因为解压过程中创建文件时有些符号会错误从而得到错误的解压路径，所以需要替换才能正常解压）
     *
     * @param wrongUnzipFilepath
     * @return
     */
    public static String rectifyUnzipPath(String wrongUnzipFilepath) {
        String unzipFilepath = wrongUnzipFilepath.replace(":", "_");
        StringBuilder stringBuilder = new StringBuilder(unzipFilepath);
        stringBuilder.replace(1, 2, ":");
        String rightPath = stringBuilder.toString();
        return rightPath;
    }

    /**
     * 按指定日期编号查找气象数据文件
     *
     * @param dateNumber
     * @param date
     * @return
     */
    public static LocalDate resolveDate(String dateNumber, String date) {
        switch (dateNumber) {
            case "1": {
                //最新数据时间是昨天，从昨天开始算

                //最新文件时间
                LocalDate dateTime = LocalDate.parse(date);
                return dateTime;
            }
            case "2": {
                //相对最新文件时间一天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(1);
                return dateTime;
            }
            case "3": {
                //相对最新文件时间两天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(2);
                return dateTime;
            }
            case "4": {
                //相对最新文件时间三天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(3);
                return dateTime;
            }
            case "5": {
                //相对最新文件时间四天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(4);
                return dateTime;
            }
            case "6": {
                //相对最新文件时间五天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(5);
                return dateTime;
            }
            case "7": {
                //相对最新文件时间六天前
                LocalDate dateTime = LocalDate.parse(date).minusDays(6);
                return dateTime;
            }
            default: {
                throw new CustomException(AppHttpCodeEnum.DATE_ERROR);
            }
        }
    }
}
