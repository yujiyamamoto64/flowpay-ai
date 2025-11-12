package com.flowpay.payment.service;

import com.flowpay.payment.integration.NotificationClient;
import com.flowpay.payment.model.PaymentResponse;
import com.flowpay.payment.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private static final String PAYER = "joao";
    private static final String PAYEE = "pedro";

    private final AccountRepository accountRepository;
    private final NotificationClient notificationClient;

    public PaymentService(AccountRepository accountRepository, NotificationClient notificationClient) {
        this.accountRepository = accountRepository;
        this.notificationClient = notificationClient;
    }

    public PaymentResponse payFromJoaoToPedro(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be greater than zero");
        }

        BigDecimal joaoBalance = accountRepository.getBalance(PAYER);
        if (joaoBalance.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient funds");
        }

        accountRepository.transfer(PAYER, PAYEE, amount);

        BigDecimal payerBalance = accountRepository.getBalance(PAYER);
        BigDecimal payeeBalance = accountRepository.getBalance(PAYEE);

        // Fire-and-forget notification to n8n (errors are logged but do not fail the payment)
        notificationClient.sendPaymentCompleted(PAYER, PAYEE, amount);

        return new PaymentResponse(PAYER, PAYEE, amount, payerBalance, payeeBalance);
    }
}
