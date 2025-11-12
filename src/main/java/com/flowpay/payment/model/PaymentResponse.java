package com.flowpay.payment.model;

import java.math.BigDecimal;

public class PaymentResponse {
    private String payer;
    private String payee;
    private BigDecimal amount;
    private BigDecimal payerBalance;
    private BigDecimal payeeBalance;

    public PaymentResponse(String payer, String payee, BigDecimal amount, BigDecimal payerBalance, BigDecimal payeeBalance) {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
        this.payerBalance = payerBalance;
        this.payeeBalance = payeeBalance;
    }

    public String getPayer() { return payer; }
    public String getPayee() { return payee; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPayerBalance() { return payerBalance; }
    public BigDecimal getPayeeBalance() { return payeeBalance; }
}
