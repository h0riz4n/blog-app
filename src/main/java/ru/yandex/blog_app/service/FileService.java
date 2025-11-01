package ru.yandex.blog_app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.properties.BlogAppProperty;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(BlogAppProperty.class)
public class FileService {

    private final BlogAppProperty property;

    public String upload(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(property.getUploadDir());

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(file.getOriginalFilename());
            file.transferTo(filePath);

            return file.getOriginalFilename();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Resource download(String filename) {
        try {
            Path filePath = Paths.get(property.getUploadDir()).resolve(filename).normalize();
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void delete(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(property.getUploadDir()).resolve(fileName).normalize());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл: " + fileName, e);
        }
    }
}
