package ru.yandex.blog_app.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.dao.CommentDao;
import ru.yandex.blog_app.model.domain.Comment;

@Repository
public class JdbcCommentDao implements CommentDao {

    private final String TABLE_NAME = "comment";
    private final String ID_COLUMN = "id";
    
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcCommentDao(
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
    public List<Comment> findAllByPostId(Long postId) {
        final String SELECT_QUERY = """
            SELECT c.*
            FROM blog_app.comment c
            WHERE c.post_id = :postId    
            """;

        return namedParameterJdbcTemplate.query(SELECT_QUERY, Map.of("postId", postId), (rs, rn) -> extract(rs));
    }

    @Override
    public Optional<Comment> findByIdAndPostId(Long id, Long postId) {
        final String SELECT_QUERY = """
            SELECT c.*
            FROM blog_app.comment c
            WHERE c.id = :id
            AND c.post_id = :postId  
            """;

        var parameters = Map.of(
            "id", id,
            "postId", postId
        );

        try {
            Comment comment = namedParameterJdbcTemplate.queryForObject(SELECT_QUERY, parameters, (rs, rn) -> extract(rs));
            return Optional.ofNullable(comment);
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Comment save(Comment comment) {
        var parameters = Map.of(
            "post_id", comment.getPostId(),
            "text", comment.getText()
        );

        comment.setId(simpleJdbcInsert.executeAndReturnKey(parameters).longValue());
        return comment;
    }

    @Override
    public void updateText(Long id, String text) {
        final String UPDATE_QUERY = """
            UPDATE blog_app.comment
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
    public void deleteByIdAndPostId(Long id, Long postId) {
        final String DELETE_QUERY = """
            DELETE FROM blog_app.comment
            WHERE id = :id
            AND post_id = :postId
            """;

        var parameters = Map.of(
            "id", id,
            "postId", postId
        );

        namedParameterJdbcTemplate.update(DELETE_QUERY, parameters);
    }

    @Override
    public Map<Long, Long> countAllByPostIds(List<Long> postIds) {
         if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        final String SELECT_QUERY = """
            SELECT c.post_id, COUNT(c.id) AS comments_count
            FROM blog_app.comment c 
            WHERE c.post_id IN (:postIds)
            GROUP BY c.post_id
            """;

        return namedParameterJdbcTemplate.query(SELECT_QUERY, Map.of("postIds", postIds), rs -> {
            Map<Long, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getLong("post_id"), rs.getLong("comments_count"));
            }
            return result;
        });
    }

    private Comment extract(ResultSet resultSet) throws SQLException {
        return Comment.builder()
            .id(resultSet.getLong("id"))
            .postId(resultSet.getLong("post_id"))
            .text(resultSet.getString("text"))
            .build();
    }
}
