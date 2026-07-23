package br.com.lztec.controllers;

import java.time.LocalDate;
import java.util.List;

// Corpo da requisição para registrar um ou mais pagamentos de uma venda.
public class RecebimentoRequest {
    private List<PagamentoLinha> pagamentos;
    // Obrigatória quando a soma dos pagamentos (novos + já existentes) fica
    // menor que o valor total da venda (ou seja, quando ela vira PARCIAL).
    private LocalDate dataVencimentoRestante;

    public List<PagamentoLinha> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoLinha> pagamentos) { this.pagamentos = pagamentos; }

    public LocalDate getDataVencimentoRestante() { return dataVencimentoRestante; }
    public void setDataVencimentoRestante(LocalDate dataVencimentoRestante) { this.dataVencimentoRestante = dataVencimentoRestante; }
}
