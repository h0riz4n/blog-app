package ru.yandex.blog_app.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "blog-app")
public class BlogAppProperty {

    private String uploadDir;

    private String[] corsOrigins;
}
