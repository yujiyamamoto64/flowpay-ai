package com.flowpay.payment.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SearchRequest {
    @NotNull
    @NotEmpty
    private List<Double> embedding;

    @Min(1)
    private int topK = 5;

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
}

