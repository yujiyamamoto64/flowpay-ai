package com.flowpay.payment.repository;

import com.flowpay.payment.model.SearchResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class DocumentRepository {
    private final JdbcTemplate jdbcTemplate;

    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UUID upsert(UUID id, String content, List<Double> embedding) {
        UUID docId = (id != null) ? id : UUID.randomUUID();
        String vec = toVectorLiteral(embedding);
        jdbcTemplate.update(
                "INSERT INTO documents (id, content, embedding) VALUES (?, ?, ?::vector) " +
                        "ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding",
                ps -> {
                    ps.setObject(1, docId);
                    ps.setString(2, content);
                    ps.setString(3, vec);
                }
        );
        return docId;
    }

    public List<SearchResult> search(List<Double> embedding, int topK) {
        String vec = toVectorLiteral(embedding);
        String sql = "SELECT id, content, (embedding <-> ?::vector) AS distance FROM documents " +
                "ORDER BY embedding <-> ?::vector LIMIT ?";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, vec);
            ps.setString(2, vec);
            ps.setInt(3, topK);
        }, (rs, rowNum) -> new SearchResult(
                (UUID) rs.getObject("id"),
                rs.getString("content"),
                rs.getDouble("distance")
        ));
    }

    private String toVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(d -> {
                    if (d == null) return "0";
                    // Ensure plain string (avoid scientific notation issues)
                    String s = d.toString();
                    return s;
                })
                .collect(Collectors.joining(",")) + "]";
    }
}

