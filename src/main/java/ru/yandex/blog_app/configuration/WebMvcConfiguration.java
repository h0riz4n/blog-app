package ru.yandex.blog_app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Setter;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Setter
    @Value("${blog-app.allowed-origins}")
    private String CORS_ORIGINS;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*") 
            .allowedMethods("*")
            .allowedHeaders("*");    
    }
}
