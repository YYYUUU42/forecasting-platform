package com.file.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
@AllArgsConstructor
@NoArgsConstructor
public class MinIOConfigProperties implements Serializable {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String readPath;

    public String getEndpoint(){
        return this.endpoint;
    }

    public String getAccessKey(){
        return this.accessKey;
    }

    public String getSecretKey(){
        return this.secretKey;
    }

    public String getBucket(){
        return this.bucket;
    }

    public String getReadPath(){
        return this.readPath;
    }
}
