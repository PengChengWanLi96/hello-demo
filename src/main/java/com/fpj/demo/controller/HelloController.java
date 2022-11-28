package com.fpj.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author fangpengjun
 * @Date 2022/6/14
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String getHello(){
        return "hello, lucky boy!";
    }
}
