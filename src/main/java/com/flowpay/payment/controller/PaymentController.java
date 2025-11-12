package com.flowpay.payment.controller;

import com.flowpay.payment.model.PaymentRequest;
import com.flowpay.payment.model.PaymentResponse;
import com.flowpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public PaymentResponse pay(@Valid @RequestBody PaymentRequest request) {
        return paymentService.payFromJoaoToPedro(request.getAmount());
    }
}
