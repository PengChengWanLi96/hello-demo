package com.fpj.demo.service;

import com.fpj.demo.config.MinIOConfiguration;
import io.minio.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

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


    /**
     * bucket下上传文件
     * @param file
     * @return
     */
    public String upload(MultipartFile file, String path){
        String bucket = minIOConfiguration.getBucket();
        String fileName = file.getOriginalFilename();
        if (null != path && !"".equals(path)){
            fileName = path+ "/" + fileName;
        }
        long fileSize = file.getSize();
        long start = System.currentTimeMillis();

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
            return String.format("上传文件失败，bucket:{}, fileName:{}", bucket, fileName);
        }
        log.info("upload file, 上传文件成功，上传耗时：{} milliseconds， bucket:{}, fileName:{}, fileByteSize:{} Byte, fileMbSize:{} MIB", System.currentTimeMillis()-start, bucket, fileName, fileSize, String.format("%.3f",fileSize*1.0/1024/1024));
        return String.format("%s/%s", bucket, fileName);
    }


    /**
     * 下载bucket下的文件，path包括bucket下的路径及文件名称
     * @param path
     * @return
     */
    public void download(String path, HttpServletRequest request, HttpServletResponse response) {
        String bucket = minIOConfiguration.getBucket();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedInputStream bufferedInputStream = null;

        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
            bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[bufferedInputStream.available()];
            if (bufferedInputStream.read(buffer) == 0) {
                log.warn("下载文件，未读取到任何内容");
            }
            // 清空response
            response.reset();

            String realname = path;
            String agent = (String) request.getHeader("USER-AGENT");
            if (agent != null && agent.toLowerCase().indexOf("firefox") > 0)//火狐浏览器下采用base64编码
            {
                realname = "=?UTF-8?B?" + (new String(Base64.encodeBase64(realname.getBytes("UTF-8")))) + "?=";
            } else {
                realname = URLEncoder.encode(realname, "UTF-8");
            }

            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + realname);
            response.addHeader("Content-Length", "" + path.length());
            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();

        } catch (Exception e) {
            log.error("download file is error, 下载文件出现错误，errMsg:{}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("download file is over, 下载文件结束， fileName:{}", path);

    }


    /**
     * 删除bucket下的文件，path包括bucket下的路径及文件名称
     * @param path
     */
    public void delete(String path){
        String bucket = minIOConfiguration.getBucket();
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket) // 存储桶
                    .object(path) // 文件名
                    .build());
        } catch (Exception e) {
            log.error("delete file is error, 删除文件出现错误，errMsg:{}", e.getMessage());
            e.printStackTrace();
            return;
        }

        log.info("delete file, 删除文件成功， bucket:{}, fileName:{}", bucket, path);
    }


}
