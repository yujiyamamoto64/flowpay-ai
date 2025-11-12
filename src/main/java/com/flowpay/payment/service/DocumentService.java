package com.flowpay.payment.service;

import com.flowpay.payment.model.DocumentUpsertRequest;
import com.flowpay.payment.model.SearchRequest;
import com.flowpay.payment.model.SearchResult;
import com.flowpay.payment.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public UUID upsert(DocumentUpsertRequest req) {
        return repository.upsert(req.getId(), req.getContent(), req.getEmbedding());
    }

    public List<SearchResult> search(SearchRequest req) {
        return repository.search(req.getEmbedding(), req.getTopK());
    }
}

