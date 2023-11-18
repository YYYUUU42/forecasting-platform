package org.example.service.impl;

import com.alibaba.excel.EasyExcel;
import com.file.service.FileStorageService;
import org.apache.commons.lang3.StringUtils;
import org.example.exception.*;
import org.example.listener.GeneralListener;
import org.example.mapper.FileMapper;
import org.example.mapper.MonitoringDataMapper;
import org.example.mapper.PointsMapper;
import org.example.model.dto.MonitoringDataDto;
import org.example.model.entity.File;
import org.example.model.entity.MonitoringData;
import org.example.model.enums.AppHttpCodeEnum;
import org.example.service.DataService;
import org.example.utils.BeanCopyUtils;
import org.example.utils.FileUtil;
import org.example.utils.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

import static jodd.io.FileUtil.mkdir;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataServiceImpl implements DataService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private MonitoringDataMapper monitoringDataMapper;

    @Resource
    private PointsMapper pointsMapper;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadFile(String latitude, String longitude, MultipartFile multipartFile) {
        //检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //上传图片到minIO中
        String fileName = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String path = null;

        //上传后得到访问路径
        try {
            path = fileStorageService.uploadFile("", fileName + postfix, multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(AppHttpCodeEnum.SERVER_ERROR);
        }

        //得到需要存放文件位置点坐标
        Integer pointId = pointsMapper.getPointId(latitude, longitude);

        if (pointId == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //将上传完的文件信息保存到数据库中
        File file = new File();
        file.setName(originalFilename);
        file.setPostfix(postfix);
        file.setPath(path);
        file.setType(FileUtil.getFileType(postfix.substring(1)));
        file.setTime(new Date(System.currentTimeMillis()));
        file.setPointId(pointId);

        fileMapper.insert(file);

        //返回可以访问的外链
        return ResponseResult.okResult(path);
    }

    /**
     * 通过excel得到监测数据
     *
     * @param file
     * @return
     */
    @Override
    public ResponseResult excelFile(String latitude, String longitude, MultipartFile file) {
        //判断有没有选择文件
        if (StringUtils.isBlank(file.getOriginalFilename())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.UNSELECTED_FILE);
        }

        // 限制文件大小
        if (!FileUtil.checkFileSize(file.getSize(), 3, "M")) {
            throw new CustomException(AppHttpCodeEnum.OVER_SIZE);
        }

        // 限制格式
        String originalFilename = file.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf(".")).substring(1);
        if (!postfix.equals("xls") && !postfix.equals("xlsx")) {
            throw new CustomException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }

        //读取excel文件
        GeneralListener<MonitoringDataDto> generalListener = new GeneralListener<>();
        try {
            EasyExcel.read(file.getInputStream(), MonitoringDataDto.class, generalListener).sheet().doRead();
        } catch (Exception e) {
            throw new CustomException(AppHttpCodeEnum.READ_EXCEL_ERROR);
        }

        //将数据存储到数据库中
        List<MonitoringDataDto> dtoList = generalListener.getList();
        List<MonitoringData> list = BeanCopyUtils.copyBeanList(dtoList, MonitoringData.class);
        for (MonitoringData monitoringData : list) {
            monitoringDataMapper.insert(monitoringData);
        }

        return ResponseResult.okResult();
    }

    /**
     * 自动化下载海洋气象动力图
     *
     * @throws Exception
     */
    /*@Scheduled(cron = "0 15 10 ? * *")//每天上午10点15分开始执行
    @Override
    public void downloadData() {
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
        String resBody = null;

        try {
            // 执行登录请求，获取token
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

        // 获取当天气象图文件的名称
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime yesterday = dateTime.minusHours(24);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fileName = yesterday.format(formatter)+"12";


        //查找最新气象图文件是否已经上传
        try {
            String dirUrl = "" + fileName + "&";
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

        try {
            //TODO 文件默认下载路径(后面再改)
            String zipPath = "D:\\\\forecasting-platform\\海洋气象动力图" + java.io.File.separator + fileName + ".zip";

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

        //设置文件解压目录
        String unZipDir = "D:\\\\forecasting-platform\\海洋气象动力图";
        //设置待解压文件路径
        String unZipPath = "D:\\\\forecasting-platform\\海洋气象动力图\\" + fileName + ".zip";

        //判断解压文件是否已存在，若存在则抛出异常
        String resultDirPath = "D:\\\\forecasting-platform\\海洋气象动力图\\" + fileName;
        java.io.File resultDir = new java.io.File(resultDirPath);

        if (resultDir.exists()) {
            throw new CustomException(AppHttpCodeEnum.DIRECTORY_IS_EXISTS);
        }

        try {
            //解压ZIP压缩包
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(unZipPath));
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    //读取到文件夹时就创建文件夹
                    String wrongUnzipFilePath = unZipDir + java.io.File.separator + entry.getName();
                    String unzipFilepath = rectifyUnzipPath(wrongUnzipFilePath);

                    mkdir(new java.io.File(unzipFilepath));
                } else {
                    // 读取到文件时就写入文件
                    String wrongUnzipFilePath = unZipDir + java.io.File.separator + entry.getName();
                    String unzipFilepath = rectifyUnzipPath(wrongUnzipFilePath);
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

        //删除压缩包
        java.io.File zipFile = new java.io.File(unZipPath);
        // 判断文件是否存在
        if (zipFile.isFile() && zipFile.exists()) {
        }    // 删除文件
        zipFile.delete();
    }*/

    /**
     * 纠正错误的解压地址（因为解压过程中创建文件时有些符号会错误从而得到错误的解压路径，所以需要替换才能正常解压）
     *
     * @param wrongUnzipFilepath
     * @return
     */
    /*private String rectifyUnzipPath(String wrongUnzipFilepath) {
        String unzipFilepath = wrongUnzipFilepath.replace(":", "_");
        StringBuilder stringBuilder = new StringBuilder(unzipFilepath);
        stringBuilder.replace(1, 2, ":");
        String rightPath = stringBuilder.toString();
        return rightPath;
    }*/

}