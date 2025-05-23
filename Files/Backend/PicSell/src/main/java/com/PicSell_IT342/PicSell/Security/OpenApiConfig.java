package com.PicSell_IT342.PicSell.Security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PicSell Endpoint Tester")
                        .version("2.0")
                        .description("PicSell Endpoint Tester API use it to see and understand the API"));
    }
}
