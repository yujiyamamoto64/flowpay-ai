package com.example.pagamento.repository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ContaRepository {
    private final Map<String, BigDecimal> saldos = new ConcurrentHashMap<>();

    public ContaRepository() {
        saldos.put("joao", new BigDecimal("1000.00"));
        saldos.put("pedro", new BigDecimal("500.00"));
    }

    public synchronized BigDecimal getSaldo(String usuario) {
        return saldos.get(usuario);
    }

    public synchronized void transferir(String de, String para, BigDecimal valor) {
        BigDecimal saldoDe = saldos.get(de);
        BigDecimal saldoPara = saldos.get(para);
        saldos.put(de, saldoDe.subtract(valor));
        saldos.put(para, saldoPara.add(valor));
    }
}

