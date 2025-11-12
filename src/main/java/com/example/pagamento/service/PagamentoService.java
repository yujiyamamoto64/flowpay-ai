package com.example.pagamento.service;

import com.example.pagamento.model.PagamentoResponse;
import com.example.pagamento.repository.ContaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PagamentoService {
    private static final String PAGADOR = "joao";
    private static final String RECEBEDOR = "pedro";

    private final ContaRepository contaRepository;

    public PagamentoService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public PagamentoResponse pagarDeJoaoParaPedro(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor deve ser maior que zero");
        }

        BigDecimal saldoJoao = contaRepository.getSaldo(PAGADOR);
        if (saldoJoao.compareTo(valor) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficiente");
        }

        contaRepository.transferir(PAGADOR, RECEBEDOR, valor);

        BigDecimal saldoPagador = contaRepository.getSaldo(PAGADOR);
        BigDecimal saldoRecebedor = contaRepository.getSaldo(RECEBEDOR);

        return new PagamentoResponse(PAGADOR, RECEBEDOR, valor, saldoPagador, saldoRecebedor);
    }
}

