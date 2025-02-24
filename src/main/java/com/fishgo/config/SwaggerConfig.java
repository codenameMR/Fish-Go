package com.fishgo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FishGo API 문서화 시스템")
                        .version("v1.0")
                        .description("FishGo 프로젝트의 REST API 문서")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}