package ru.yandex.blog_app.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.dao.PostDao;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.model.dto.Page;

@Repository
public class JdbcPostDao implements PostDao {

    private final String TABLE_NAME = "post";
    private final String ID_COLUMN = "id";
    
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcPostDao(
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
    public Post save(Post post) {
        var parameters = Map.of(
            "title", post.getTitle(),
            "text", post.getText(),
            "likes_count", 0
        );
        post.setId(simpleJdbcInsert.executeAndReturnKey(parameters).longValue());
        return post;
    }
    
    @Override
    public Page<Post> findAll(String title, List<String> tagsText, Integer pageNumber, Integer pageSize) {
        final String SELECT_QUERY = """
            SELECT p.id, p.title, p.text, p.likes_count
            FROM blog_app.post p
            LEFT JOIN blog_app.tag t
            ON p.id = t.post_id
            WHERE p.title LIKE CONCAT('%', :title, '%')
            GROUP BY p.id
            HAVING COUNT(DISTINCT CASE WHEN t.text IN (:tags) THEN t.text END) = :tagCount
            LIMIT :limit
            OFFSET :offset
            """;

        var parameters = Map.of(
            "title", title,
            "tagCount", tagsText.size(),
            "tags", tagsText,
            "limit", pageSize,
            "offset", pageNumber * pageSize
        );

        List<Post> posts = namedParameterJdbcTemplate.query(SELECT_QUERY, parameters, (rs, rn) -> {
            return Post.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .text(rs.getString("text"))
                .likesCount(rs.getLong("likes_count"))
                .build();
        });  
      
        long totalPages = (countAll(title, tagsText) + pageSize - 1) / pageSize;
        long lastPage = totalPages == 0 ? 0 : totalPages - 1;

        return Page.<Post>builder()
            .posts(posts)
            .lastPage(lastPage)
            .hasPrev(pageNumber > 0)
            .hasNext(pageNumber < lastPage)
            .build();
    }

    @Override
    public Optional<Post> findById(Long id) {
        final String SELECT_QUERY = """
            SELECT 
                p.*, 
                COUNT(c.id) AS comments_count,
                t.id AS tag_id,
                t.post_id AS post_id,
                t.text AS tag_text
            FROM blog_app.post p
            LEFT JOIN blog_app.comment c
            ON p.id = c.post_id
            LEFT JOIN blog_app.tag t
            ON p.id = t.post_id
            WHERE p.id = :id
            GROUP BY p.id, t.id
            """;

        try {
            Post post = namedParameterJdbcTemplate.query(SELECT_QUERY, Map.of("id", id), this::extractPost);
            return Optional.ofNullable(post);
        } catch (DataAccessException ex) {
            throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        final String DELETE_QUERY = """
            DELETE FROM blog_app.post
            WHERE id = :id
            """;

        namedParameterJdbcTemplate.update(DELETE_QUERY, Map.of("id", id));
    }

    @Override
    public void updateText(Long id, String text) {
        final String UPDATE_QUERY = """
            UPDATE blog_app.post
            SET text = :text
            WHERE id = :id
            """;

        var parameters = Map.of(
            "text", text,
            "id", id
        );

        namedParameterJdbcTemplate.update(UPDATE_QUERY, parameters);
    }

    @Override
    public void updateLikesCount(Long id, Long likesCount) {
        final String UPDATE_QUERY = """
            UPDATE blog_app.post
            SET likes_count = :likesCount
            WHERE id = :id
            """;

        var parameters = Map.of(
            "likesCount", likesCount,
            "id", id
        );

        namedParameterJdbcTemplate.update(UPDATE_QUERY, parameters);
    }

    
    @Override
    public void updateFileName(Long id, String fileName) {
        final String UPDATE_QUERY = """
            UPDATE blog_app.post
            SET file_name = :fileName
            WHERE id = :id
            """;

        var parameters = Map.of(
            "fileName", fileName,
            "id", id
        );

        namedParameterJdbcTemplate.update(UPDATE_QUERY, parameters);
    }

    @Override
    public Optional<String> findFileNameById(Long id) {
        final String SELECT_QUERY = """
            SELECT p.file_name
            FROM blog_app.post p
            WHERE p.id = :id
            """;

         try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SELECT_QUERY, Map.of("id", id), String.class));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    private Long countAll(String title, List<String> tagsText) {
        final String COUNT_QUERY = """
            SELECT COUNT(p.id)
            FROM blog_app.post p
            LEFT JOIN blog_app.tag t
            ON p.id = t.post_id
            WHERE p.title LIKE CONCAT('%', :title, '%')
            GROUP BY p.id
            HAVING (:tagCount = 0 or COUNT(DISTINCT CASE WHEN t.text IN (:tags) THEN t.text END) = :tagCount)
            """;

        var parameters = Map.of(
            "title", title,
            "tagCount", tagsText.size(),
            "tags", tagsText
        );

        return namedParameterJdbcTemplate.queryForObject(COUNT_QUERY, parameters, Long.class);
    }

    private Post extractPost(ResultSet resultSet) throws SQLException {
        Map<Long, Post> posts = new LinkedHashMap<>();

        while (resultSet.next()) {
            Long postId = resultSet.getLong("id");
            Post post = posts.computeIfAbsent(postId, id -> getPost(resultSet));

            Optional.ofNullable(resultSet.getLong("tag_id"))
                .ifPresent(tagId ->  post.getTags().add(getTag(tagId, resultSet)));
        }

        return posts.values().stream().findFirst().orElse(null);
    }

    private Post getPost(ResultSet resultSet) {
        try {
            return Post.builder()
                .id(resultSet.getLong(ID_COLUMN))
                .title(resultSet.getString("title"))
                .text(resultSet.getString("text"))
                .likesCount(resultSet.getLong("likes_count"))
                .commentsCount(resultSet.getLong("comments_count"))
                .fileName(resultSet.getString("file_name"))
                .build();
        } catch (SQLException ex) {
            throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Tag getTag(Long tagId, ResultSet resultSet) {
        try {
            return Tag.builder()
                .id(tagId)
                .postId(resultSet.getLong("post_id"))
                .text(resultSet.getString("tag_text"))
                .build();
        } catch (SQLException ex) {
            throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
