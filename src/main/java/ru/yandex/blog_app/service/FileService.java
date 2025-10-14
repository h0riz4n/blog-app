package ru.yandex.blog_app.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String upload(MultipartFile file);

    Resource download(String filename);
}
