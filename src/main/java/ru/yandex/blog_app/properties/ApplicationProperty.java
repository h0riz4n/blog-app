package ru.yandex.blog_app.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationProperty {

    /**
     * Информация о проекте
     */
    private Info info;

    /**
     * Сервер запросов
     */
    private Server server;

    /**
     * Класс информации для документации проекта
     */
    @Getter
    @Setter
    public static class Info {
        /**
         * Название проекта
         */
        private String title;

        /**
         * Версия проекта
         */
        private String version;

        /**
         * Описание проекта
         */
        private String description;

    }

    /**
     * Класс сервера для документации проекта
     */
    @Getter
    @Setter
    public static class Server {
        /**
         * Ссылка на сервер
         */
        private String url;

        /**
         * Описание сервера
         */
        private String description;
    }

}
