package com.PicSell_IT342.PicSell.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}