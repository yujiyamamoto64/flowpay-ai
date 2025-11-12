package com.flowpay.payment.controller;

import com.flowpay.payment.model.DocumentUpsertRequest;
import com.flowpay.payment.model.SearchRequest;
import com.flowpay.payment.model.SearchResult;
import com.flowpay.payment.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> upsert(@Valid @RequestBody DocumentUpsertRequest request) {
        UUID id = service.upsert(request);
        return Map.of("id", id);
    }

    @PostMapping("/search")
    public List<SearchResult> search(@Valid @RequestBody SearchRequest request) {
        return service.search(request);
    }
}

