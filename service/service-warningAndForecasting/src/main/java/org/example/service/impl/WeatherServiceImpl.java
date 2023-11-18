package org.example.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.file.service.FileStorageService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.exception.CustomException;
import org.example.mapper.WeatherDataMapper;
import org.example.model.entity.WeatherData;
import org.example.model.enums.AppHttpCodeEnum;
import org.example.service.WeatherService;
import org.example.model.vo.WeatherVO;
import org.example.utils.WeatherUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static jodd.io.FileUtil.mkdir;

@Service
@Transactional(rollbackFor = Exception.class)
public class WeatherServiceImpl implements WeatherService {


    @Resource
    private WeatherDataMapper weatherDataMapper;


    /**
     * 自动化下载海洋气象动力图
     *
     * @throws Exception
     */
    @Scheduled(cron = "0 15 10 ? * *")//每天上午10点15分开始执行
    @Override
    public void downloadData() {
        // 获取当天气象图文件的名称
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime yesterday = dateTime.minusHours(24);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fileName = yesterday.format(formatter) + "12";


        //设置解压文件路径
        String folderPath = "D:\\code\\\\forecasting-platform\\海洋气象动力图\\" + fileName;
        //设置文件解压目录
        String unZipDir = "D:\\code\\\\forecasting-platform\\海洋气象动力图";
        //设置待解压文件路径
        String unZipPath = "D:\\code\\\\forecasting-platform\\海洋气象动力图\\" + fileName + ".zip";
        //按解压目标路径创建解压文件
        java.io.File resultDir = new java.io.File(folderPath);

        //判断解压文件是否已存在，若存在则直接上传
        if (resultDir.exists()) {
            //将文件夹的图片外链写入数据库
        WeatherUtil.inputDatabase(fileName, folderPath);
        } else {
            //解压文件不存在,执行下载zip文件逻辑

            //创建http请求客户端
            CloseableHttpClient client = null;
            try {
                // 密码校验
                String url = "";
                client = HttpClients.createDefault();
                HttpGet check = new HttpGet(url);
                client.execute(check);
            } catch (IOException e) {
                throw new CustomException(AppHttpCodeEnum.CHECK_PASSWORD_ERROR);
            }

            //查找最新气象图文件是否已经上传
            String resBody = null;
            try {
                String dirUrl = "" + fileName + "";
                HttpGet confirmDirStatus = new HttpGet(dirUrl);
                CloseableHttpResponse response = client.execute(confirmDirStatus);
                HttpEntity entity = response.getEntity();
                resBody = EntityUtils.toString(entity, "UTF-8");
                JSONObject dirStatus = JSON.parseObject(resBody);
                Integer code = dirStatus.getInteger("code");
                if (code == 1022) {
                    throw new CustomException(AppHttpCodeEnum.ZIP_NOT_EXISTS);
                }
            } catch (IOException e) {
                throw new CustomException(AppHttpCodeEnum.SEARCH_ZIP_ERROR);
            }

            // 执行登录请求，获取token
            try {
                String loginUrl = "";
                HttpGet login = new HttpGet(loginUrl);
                CloseableHttpResponse execute = client.execute(login);
                HttpEntity entity = execute.getEntity();
                resBody = EntityUtils.toString(entity, "UTF-8");
            } catch (IOException e) {
                throw new CustomException(AppHttpCodeEnum.LOGIN_URL_ERROR);
            }

            //解析JSON，获取token（后面要作为下载的参数用）
            JSONObject jsonObject = JSON.parseObject(resBody);
            JSONObject dataJson = jsonObject.getJSONObject("data");
            JSONObject userInfoJson = dataJson.getJSONObject("userInfo");
            String token = userInfoJson.getString("fileTransferToken");

            //开始下载zip压缩包
            try {
                //TODO 文件默认下载路径(后面再改)
                String zipPath = "D:\\code\\forecasting-platform\\海洋气象动力图" + java.io.File.separator + fileName + ".zip";

                String downloadUrl = "" + fileName + "&token=" + token;
                HttpGet download = new HttpGet(downloadUrl);
                HttpResponse response = client.execute(download);
                InputStream inputStream = response.getEntity().getContent();

                FileOutputStream fos = new FileOutputStream(zipPath);
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

                byte[] byt = new byte[1024];
                int ch;
                while ((ch = inputStream.read(byt)) > 0) {
                    byteArray.write(byt, 0, ch);
                    byteArray.flush();
                }
                fos.write(byteArray.toByteArray());
                inputStream.close();
                fos.close();
                byteArray.close();
            } catch (IOException e) {
                throw new CustomException(AppHttpCodeEnum.DOWNLOAD_ZIP_ERROR);
            }

            try {
                //解压ZIP压缩包
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(unZipPath));
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    if (entry.isDirectory()) {
                        //读取到文件夹时就创建文件夹
                        String wrongUnzipFilePath = unZipDir + java.io.File.separator + entry.getName();
                        String unzipFilepath = WeatherUtil.rectifyUnzipPath(wrongUnzipFilePath);

                        mkdir(new java.io.File(unzipFilepath));
                    } else {
                        // 读取到文件时就写入文件
                        String wrongUnzipFilePath = unZipDir + java.io.File.separator + entry.getName();
                        String unzipFilepath = WeatherUtil.rectifyUnzipPath(wrongUnzipFilePath);
                        java.io.File file = new java.io.File(unzipFilepath);
                        // 创建父目录
                        mkdir(file.getParentFile());
                        // 写出文件流
                        BufferedOutputStream bufferedOutputStream =
                                new BufferedOutputStream(new FileOutputStream(unzipFilepath));
                        byte[] bytes = new byte[1024];
                        int readLen;
                        while ((readLen = zipInputStream.read(bytes)) != -1) {
                            bufferedOutputStream.write(bytes, 0, readLen);
                        }
                        bufferedOutputStream.close();
                    }
                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }

                zipInputStream.close();
            } catch (IOException e) {
                throw new CustomException(AppHttpCodeEnum.UNZIP_ERROR);
            }

            //将图片外链写入数据库
            WeatherUtil.inputDatabase(fileName, folderPath);

        }

        //删除压缩包
        java.io.File zipFile = new java.io.File(unZipPath);
        // 判断zip文件是否存在
        if (zipFile.isFile() && zipFile.exists()) {
            // 删除文件
            zipFile.delete();
        }
    }

    /**
     * 按指定日期编号查询最近七天特定日期的气象图数据
     */
    @Override
    public Map<String, WeatherVO> searchWeatherData(String dateNumber) {
        //创建WeatherData对象用于查找
        WeatherData weatherData = new WeatherData();
        List<Integer> dateNumList = new ArrayList<>();
        //按指定weatherData对象中的属性查找数据，返回WeatherVO集合
        List<WeatherVO> weatherVOList = weatherDataMapper.searchData(weatherData);

        Map<String, WeatherVO> map = new TreeMap<>();
        //给日期编号
        for (int i = 1; i <= weatherVOList.size(); i++) {
            dateNumList.add(i);
            map.put(String.valueOf(i), weatherVOList.get(i - 1));
        }
        //遍历并按编号查找最近七天特定日期的气象图数据，返回resultMap集合
        Iterator<Map.Entry<String, WeatherVO>> iterator = map.entrySet().iterator();
        Map<String, WeatherVO> resultMap = new TreeMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, WeatherVO> entry = iterator.next();
            String key = entry.getKey();
            WeatherVO value = entry.getValue();
            if (dateNumber.equals(key)) {
                resultMap.put(key, value);
                return resultMap;
            }
        }
        throw new CustomException(AppHttpCodeEnum.NOT_FOUND_DATA);
    }



}