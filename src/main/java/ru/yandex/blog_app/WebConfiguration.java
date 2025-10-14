package ru.yandex.blog_app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "ru.yandex.blog_app")
@PropertySource("classpath:application.properties")
public class WebConfiguration {

}
