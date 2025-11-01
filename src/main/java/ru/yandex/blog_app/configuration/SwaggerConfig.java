package ru.yandex.blog_app.configuration;


import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.properties.ApplicationProperty;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApplicationProperty.class)
public class SwaggerConfig {
    
    private final ApplicationProperty appProperty;

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title(appProperty.getInfo().getTitle())
                    .version(appProperty.getInfo().getVersion())
                    .description(appProperty.getInfo().getDescription())
            )
            .servers(
                List.of(
                    new Server()
                        .url(appProperty.getServer().getUrl())
                        .description(appProperty.getServer().getDescription())
                )
            );
    }

}
