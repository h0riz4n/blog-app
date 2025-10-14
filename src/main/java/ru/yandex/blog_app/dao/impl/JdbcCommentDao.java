package ru.yandex.blog_app.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.dao.CommentDao;
import ru.yandex.blog_app.model.domain.Comment;

@Repository
public class JdbcCommentDao implements CommentDao {

    private final String SCHEMA_NAME = "blog_app";
    private final String TABLE_NAME = "comment";
    private final String ID_COLUMN = "id";
    
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcCommentDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
            .withSchemaName(SCHEMA_NAME)
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
            SET text = ?
            WHERE id = ?    
            """;

        namedParameterJdbcTemplate.getJdbcTemplate().update(UPDATE_QUERY, text, id);
    }

    private Comment extract(ResultSet resultSet) throws SQLException {
        return Comment.builder()
            .id(resultSet.getLong("id"))
            .postId(resultSet.getLong("post_id"))
            .text(resultSet.getString("text"))
            .build();
    }

    @Override
    public void deleteByIdAndPostId(Long id, Long postId) {
        final String DELETE_QUERY = """
            DELETE FROM blog_app.comment
            WHERE id = ?
            AND post_id = ?    
            """;

        namedParameterJdbcTemplate.getJdbcTemplate().update(DELETE_QUERY, id, postId);
    }
}
