package com.fpj.demo.service;

import com.fpj.demo.config.MinIOConfiguration;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description
 * @Author fangpengjun
 * @Date 2022/11/29
 */

@Service
public class MinIOService {

    private static final Logger log = LoggerFactory.getLogger(MinIOService.class);

    @Autowired
    private MinIOConfiguration minIOConfiguration;

    @Autowired
    private MinioClient minioClient;



    public String upload(MultipartFile file){
        String bucket = minIOConfiguration.getBucket();
        String fileName = file.getName();

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)  // 存储桶
                    .object(fileName)                        // 文件名
                    .stream(file.getInputStream(), file.getSize(), -1)  // 文件内容
                    .contentType(file.getContentType())                          // 文件类型
                    .build());
        } catch (Exception e) {
            log.error("upload file is error, 上传文件出现错误，errMsg:{}", e.getMessage());
            e.printStackTrace();
        }
        log.info("upload file, 上传文件成功， bucket:{}, fileName:{}", bucket, fileName);
        return String.format("%s/%s", bucket, fileName);
    }


    public void delete(String path){
        String bucket = minIOConfiguration.getBucket();
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket) // 存储桶
                    .object(path) // 文件名
                    .build());
        } catch (Exception e) {
            log.error("delete file is error, 下载文件出现错误，errMsg:{}", e.getMessage());
            e.printStackTrace();
        }

        log.info("delete file, 删除文件成功， bucket:{}, fileName:{}", bucket, path);
    }


}
