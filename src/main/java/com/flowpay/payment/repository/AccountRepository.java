package com.flowpay.payment.repository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {
    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

    public AccountRepository() {
        balances.put("joao", new BigDecimal("1000.00"));
        balances.put("pedro", new BigDecimal("500.00"));
    }

    public synchronized BigDecimal getBalance(String user) {
        return balances.get(user);
    }

    public synchronized void transfer(String from, String to, BigDecimal amount) {
        BigDecimal fromBalance = balances.get(from);
        BigDecimal toBalance = balances.get(to);
        balances.put(from, fromBalance.subtract(amount));
        balances.put(to, toBalance.add(amount));
    }
}
