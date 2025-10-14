package ru.yandex.blog_app.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
    
    private List<T> posts;

    private Boolean hasPrev;

    private Boolean hasNext;

    private Long lastPage;
}
