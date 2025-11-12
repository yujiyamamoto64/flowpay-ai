package com.flowpay.payment.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public NotificationClient(
            RestTemplate restTemplate,
            @Value("${n8n.webhook.url:http://localhost:5678/webhook/event/payment.completed}") String webhookUrl
    ) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
    }

    public void sendPaymentCompleted(String payerId, String receiverId, BigDecimal amount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("payerId", payerId);
            payload.put("receiverId", receiverId);
            payload.put("amount", amount);
            ResponseEntity<String> resp = restTemplate.postForEntity(webhookUrl, payload, String.class);
            log.info("Sent n8n notification to {} -> status {}", webhookUrl, resp.getStatusCode());
        } catch (Exception e) {
            log.warn("Failed to send n8n notification to {}: {}", webhookUrl, e.getMessage());
        }
    }
}

