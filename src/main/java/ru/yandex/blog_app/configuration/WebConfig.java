package ru.yandex.blog_app.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.properties.BlogAppProperty;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BlogAppProperty.class)
public class WebConfig implements WebMvcConfigurer  {

    private final BlogAppProperty property;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(property.getCorsOrigins()) 
            .allowedMethods("*")
            .allowedHeaders("*");    
    }
}
