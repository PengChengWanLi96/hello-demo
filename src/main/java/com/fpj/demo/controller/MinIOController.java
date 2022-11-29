package com.fpj.demo.controller;

import com.fpj.demo.service.MinIOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author fangpengjun
 * @Date 2022/11/29
 */
@RestController
@RequestMapping("/minio")
public class MinIOController {

    @Autowired
    private MinIOService minIOService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file){
        return minIOService.upload(file);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("delete")
    public void deleteFile(@RequestParam("path") String path){
        minIOService.delete(path);
    }


}
