package com.fpj.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author:FangPengJun
 * @Date:2021/8/18
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${swagger.enable:true}")
    private Boolean swaggerEnable;

    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerEnable)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.fpj.demo.controller"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("SpringBoot MinIo练习项目")
                        .description("MinIo使用项目......")
                        .version("1.0")
                        //.contact(new Contact("demo","blog.csdn.net","fangpj1981@163.com"))
                        //.license("The Apache License")
                        //.licenseUrl("http://www.baidu.com")
                        .build());
    }
}
