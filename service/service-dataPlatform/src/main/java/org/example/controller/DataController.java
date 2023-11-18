package org.example.controller;

import org.example.service.DataService;
import org.example.utils.ResponseResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/service/serviceDatePlatform/Data")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataController {

    @Resource
    private DataService dataService;

    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping("/upload/{latitude}/{longitude}")
    public ResponseResult upload(@PathVariable("latitude")String latitude, @PathVariable("longitude")String longitude,MultipartFile file){
        return dataService.uploadFile(latitude,longitude,file);
    }

    /**
     * 通过excel得到监测数据
     * @param file
     * @return
     */
    @PostMapping("/excel/{latitude}/{longitude}")
    public ResponseResult excel(@PathVariable("latitude")String latitude, @PathVariable("longitude")String longitude,MultipartFile file){
        return dataService.excelFile(latitude,longitude,file);
    }

    /**
     * 自动化下载气象图
     * @return
     * @throws IOException
     */
    /*@GetMapping("/autoDownload")
    public ResponseResult AutoDownloadData() throws Exception {
            dataService.downloadData();
        return ResponseResult.okResult();
    }*/
}
