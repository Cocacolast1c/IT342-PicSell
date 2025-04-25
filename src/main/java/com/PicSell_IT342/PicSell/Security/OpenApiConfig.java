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
                        .title("Scribbie Endpoint Tester")
                        .version("1.0")
                        .description("Scribbie Endpoint Tester API use it to see and understand the API"));
    }
}
