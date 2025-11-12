package com.flowpay.payment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class DocumentUpsertRequest {
    private UUID id; // optional; if null, server will generate one

    @NotBlank
    private String content;

    @NotNull
    @NotEmpty
    private List<Double> embedding; // must match table dimension

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
}

