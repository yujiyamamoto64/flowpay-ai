package com.flowpay.payment.model;

import java.util.UUID;

public class SearchResult {
    private UUID id;
    private String content;
    private Double distance;

    public SearchResult(UUID id, String content, Double distance) {
        this.id = id;
        this.content = content;
        this.distance = distance;
    }

    public UUID getId() { return id; }
    public String getContent() { return content; }
    public Double getDistance() { return distance; }
}

