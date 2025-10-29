package ru.yandex.blog_app.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.PostEntity_;
import ru.yandex.blog_app.model.entity.TagEntity_;
import ru.yandex.blog_app.model.filter.PostFilterModel;

@RequiredArgsConstructor
public class PostSpecification implements Specification<PostEntity> {

    @NonNull
    private final PostFilterModel filter;

    @Override
    public Predicate toPredicate(Root<PostEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        filter.getTitle()
            .filter(title -> !title.isBlank())
            .ifPresent(title -> {
                predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(PostEntity_.TITLE)), "%" + title.toLowerCase() + "%")
                );
            });

        filter.getTags()
            .filter(tags -> !tags.stream().filter(Objects::nonNull).toList().isEmpty())
            .ifPresent(tags -> {
                var tagJoin = root.join(PostEntity_.TAGS, JoinType.LEFT);
                predicates.add(tagJoin.get(TagEntity_.TEXT).in(tags));
                Predicate predicate = criteriaBuilder.equal(
                    criteriaBuilder.countDistinct(tagJoin.get(TagEntity_.TEXT)), 
                    tags.size()
                );
                query.having(predicate);
                query.groupBy(root.get(PostEntity_.ID));
            });

        return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
