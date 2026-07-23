package br.com.lztec.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// Um pagamento recebido de uma venda. Uma venda pode ter vários pagamentos:
// seja porque foi dividido em mais de uma forma no mesmo dia (ex: metade Pix,
// metade Dinheiro), seja porque foi uma entrada + o restante pago depois.
@Entity
@Table(name = "venda_pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "venda_id", nullable = false)
    private Integer vendaId;

    // DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO
    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento;

    @Column(nullable = false)
    private Double valor;

    // Só relevante quando formaPagamento = CARTAO_CREDITO
    @Column(name = "numero_parcelas")
    private Integer numeroParcelas = 1;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento = LocalDate.now();

    public Pagamento() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Integer getNumeroParcelas() { return numeroParcelas; }
    public void setNumeroParcelas(Integer numeroParcelas) { this.numeroParcelas = (numeroParcelas != null && numeroParcelas > 0) ? numeroParcelas : 1; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento != null ? dataPagamento : LocalDate.now(); }
}
