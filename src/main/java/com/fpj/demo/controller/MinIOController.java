package com.fpj.demo.controller;

import com.fpj.demo.service.MinIOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(name = "path", required = false, defaultValue = "") String path){
        return minIOService.upload(file, path);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam(name = "path") String path, HttpServletRequest request, HttpServletResponse response){
        minIOService.download(path, response);
    }

    /**
     * 查询bucket下所有文件安装包
     */
    @GetMapping("/list_file")
    public Object listFile(){
        return minIOService.listBucketFiles();
    }

    /**
     * 删除文件
     */
    @DeleteMapping("delete")
    public void deleteFile(@RequestParam("path") String path){
        minIOService.delete(path);
    }


}
