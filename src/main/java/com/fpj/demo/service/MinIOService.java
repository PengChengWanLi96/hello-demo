package com.fpj.demo.service;

import com.fpj.demo.config.MinIOConfiguration;
import io.minio.*;
import io.minio.messages.Item;
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
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

            //校验存储桶是否存在
            boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if(!isExit){
                //创建一个名为xxn的存储桶，用于存储上传的文件
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("upload file create bucket is success, 上传文件bucket不存在，创建bucket：{}, fileName:{}", bucket, fileName);
            }

            //上传文件
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
        log.info("upload file, 上传文件成功，上传耗时：{} milliseconds， bucket:{}, fileName:{}, fileByteSize:{} Byte, fileMIBSize:{} MIB", System.currentTimeMillis()-start, bucket, fileName, fileSize, String.format("%.3f",fileSize*1.0/1024/1024));
        return String.format("%s/%s", bucket, fileName);
    }


    /**
     * 下载bucket下的文件，path包括bucket下的路径及文件名称
     * @return
     *//*
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

    }*/

    public void download(String filename, HttpServletResponse response){
        String bucket = minIOConfiguration.getBucket();

        try {
            //下载文件流
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(filename).build());

            BufferedInputStream ism = new BufferedInputStream(stream);
            byte[] buf = new byte[1024];
            int i=0;
            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(filename,"utf-8"));
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Methods","GET");
            response.setHeader("Access-Control-Allow-Headers",":x-requested-with,content-type");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("utf-8");
            BufferedOutputStream osm=new BufferedOutputStream(response.getOutputStream());
            while ((i=ism.read(buf))>0){
                osm.write(buf,0,i);
            }
            osm.flush();
            osm.close();
        } catch (Exception e) {
            log.error("download file is error,下载文件出现错误， fileName:{}, errMsg:{}", filename, e.getMessage());
            e.printStackTrace();
        }
        log.info("download file is success, 下载文件成功， fileName:{}", filename);
    }

    /**
     * 查询bucket下的所有安装包文件
     * @return
     */
    public List listBucketFiles(){
        String bucket = minIOConfiguration.getBucket();
        List<Map<String, Object>> list = new ArrayList();
        try {
            // 检查bucket是否存在。
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                log.error("list bucket files, 查询bucket下所有对象列表出错，不存在bucket:{}",  bucket);
                return Collections.emptyList();
            }
            // 列出bucket里的对象
            Iterable<Result<Item>> myObjects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).build());
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                Map<String, Object> fileItemObjMap =  new HashMap<>();
                if (item.isDir()){
                    fileItemObjMap.put("fileName", item.objectName());
                    list.add(fileItemObjMap);
                    continue;
                }
                fileItemObjMap.put("fileName", item.objectName());
                fileItemObjMap.put("fileBytes", item.size());
                fileItemObjMap.put("fileByteSize", String.format("%d Byte", item.size()));
                fileItemObjMap.put("fileMIBSize", String.format("%.3f MIB",item.size()*1.0/1024/1024));
                fileItemObjMap.put("lastModified", item.lastModified());
                //System.out.println(item.lastModified() + ", " + item.size() + ", " + item.objectName());
                list.add(fileItemObjMap);
            }
        } catch (Exception e) {
            log.error("list bucket files, 查询bucket下所有对象列表出错，不存在bucket:{}, errMsg:{}",  bucket, e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
        return list;
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
