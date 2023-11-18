package org.example.service;

import org.example.utils.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DataService {

    /**
     * 上传文件
     * @param file
     * @return
     */
    ResponseResult uploadFile(String latitude,String longitude,MultipartFile file);

    /**
     * 通过excel得到监测数据
     * @param file
     * @return
     */
    ResponseResult excelFile(String latitude,String longitude,MultipartFile file);

    /**
     * 自动化下载前气象图
     */
    //void downloadData() throws IOException, Exception;
}
