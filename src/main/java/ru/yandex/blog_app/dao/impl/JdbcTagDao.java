package ru.yandex.blog_app.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.dao.TagDao;
import ru.yandex.blog_app.model.domain.Tag;

@Repository
public class JdbcTagDao implements TagDao {

    private final String TABLE_NAME = "tag";
    private final String ID_COLUMN = "id";

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcTagDao(
        @Value("${blog-app.default-schema-name}") String schemaName,
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
            .withSchemaName(schemaName)
            .withTableName(TABLE_NAME)
            .usingGeneratedKeyColumns(ID_COLUMN);
    }

    @Override
    public List<Tag> saveAll(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        tags.forEach(tag -> {
            var parameters = Map.of(
                "post_id", tag.getPostId(),
                "text", tag.getText()
            );

            tag.setId(simpleJdbcInsert.executeAndReturnKey(parameters).longValue());
        });

        return tags;
    }

    @Override
    public Map<Long, List<Tag>> findAllByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        final String SELECT_QUERY = """
            SELECT t.*
            FROM blog_app.tag t
            WHERE t.post_id IN (:postIds)
            """;

        List<Tag> tags = namedParameterJdbcTemplate.query(SELECT_QUERY, Map.of("postIds", postIds), (rs, rowNum) -> {
            return Tag.builder()
                .id(rs.getLong("id"))
                .postId(rs.getLong("post_id"))
                .text(rs.getString("text"))
                .build();
        });

        return tags.stream().collect(Collectors.groupingBy(Tag::getPostId));
    }
}
