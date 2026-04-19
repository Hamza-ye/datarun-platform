package dev.datarun.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExpressionRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ExpressionRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void insert(ExpressionRule rule) {
        jdbc.update("""
                INSERT INTO expression_rules (id, activity_ref, shape_ref, field_name, rule_type, expression, message)
                VALUES (?::uuid, ?, ?, ?, ?, ?::jsonb, ?)
                """,
                rule.id().toString(),
                rule.activityRef(),
                rule.shapeRef(),
                rule.fieldName(),
                rule.ruleType(),
                toJson(rule.expression()),
                rule.message());
    }

    public Optional<ExpressionRule> findById(UUID id) {
        List<ExpressionRule> results = jdbc.query("""
                SELECT id, activity_ref, shape_ref, field_name, rule_type, expression, message, created_at
                FROM expression_rules WHERE id = ?::uuid
                """,
                ruleRowMapper(), id.toString());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<ExpressionRule> findByActivityAndShape(String activityRef, String shapeRef) {
        return jdbc.query("""
                SELECT id, activity_ref, shape_ref, field_name, rule_type, expression, message, created_at
                FROM expression_rules WHERE activity_ref = ? AND shape_ref = ?
                ORDER BY field_name, rule_type
                """,
                ruleRowMapper(), activityRef, shapeRef);
    }

    public List<ExpressionRule> findByActivityAndShapeAndField(String activityRef, String shapeRef, String fieldName) {
        return jdbc.query("""
                SELECT id, activity_ref, shape_ref, field_name, rule_type, expression, message, created_at
                FROM expression_rules WHERE activity_ref = ? AND shape_ref = ? AND field_name = ?
                ORDER BY rule_type
                """,
                ruleRowMapper(), activityRef, shapeRef, fieldName);
    }

    public List<ExpressionRule> findAll() {
        return jdbc.query("""
                SELECT id, activity_ref, shape_ref, field_name, rule_type, expression, message, created_at
                FROM expression_rules ORDER BY activity_ref, shape_ref, field_name, rule_type
                """,
                ruleRowMapper());
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM expression_rules WHERE id = ?::uuid", id.toString());
    }

    private RowMapper<ExpressionRule> ruleRowMapper() {
        return (rs, rowNum) -> {
            try {
                return new ExpressionRule(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("activity_ref"),
                        rs.getString("shape_ref"),
                        rs.getString("field_name"),
                        rs.getString("rule_type"),
                        objectMapper.readTree(rs.getString("expression")),
                        rs.getString("message"),
                        rs.getTimestamp("created_at").toInstant());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse expression JSON", e);
            }
        };
    }

    private String toJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}
