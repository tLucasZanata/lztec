package br.com.lztec.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cabeçalho de uma venda: cliente + data + status de pagamento.
// Os produtos vendidos ficam na tabela venda_item (várias linhas por venda),
// e os pagamentos recebidos ficam na tabela venda_pagamento (uma venda pode
// ter vários pagamentos, ex: entrada + restante depois, ou dividido em mais
// de uma forma no mesmo dia). Ambos manipulados pelo VendaServiceImpl.
@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda = LocalDate.now();

    // PENDENTE (nada recebido ainda), PARCIAL (recebeu uma entrada, falta o
    // resto) ou PAGO (recebeu o valor total).
    @Column(nullable = false)
    private String status = "PENDENTE";

    private String observacao;

    // Desconto geral aplicado sobre o total da venda (soma dos itens já com
    // seus descontos individuais), opcional. Mesma lógica do desconto por
    // item: valorDescontoGeral é o valor em R$, percentualDescontoGeral é o
    // equivalente em %, mantido em sincronia pelo backend.
    @Column(name = "percentual_desconto_geral")
    private Double percentualDescontoGeral = 0.0;

    @Column(name = "valor_desconto_geral")
    private Double valorDescontoGeral = 0.0;

    // Data prevista pro pagamento do restante, quando a venda está PARCIAL
    // (ex: cliente deu uma entrada e vai pagar o resto em outro dia).
    @Column(name = "data_vencimento_restante")
    private LocalDate dataVencimentoRestante;

    @Transient
    private List<VendaItem> itens = new ArrayList<>();

    // Pagamentos já recebidos desta venda (uma ou mais linhas: forma +
    // valor + parcelas). Preenchido pelo service a partir da tabela
    // venda_pagamento.
    @Transient
    private List<Pagamento> pagamentos = new ArrayList<>();

    // Apenas para exibição: nome do cliente (preenchido pelo service)
    @Transient
    private String nomeCliente;

    // Apenas para exibição: telefone e endereço do cliente (para o comprovante)
    @Transient
    private String telefoneCliente;

    @Transient
    private String enderecoCliente;

    // Soma dos itens já com desconto individual, ANTES do desconto geral
    @Transient
    private Double valorBruto;

    // Soma dos itens com desconto individual, MENOS o desconto geral (valor final da venda)
    @Transient
    private Double valorTotal;

    // Soma de todos os pagamentos já recebidos desta venda
    @Transient
    private Double valorPago;

    // valorTotal - valorPago (quanto ainda falta receber)
    @Transient
    private Double saldoDevedor;

    public Venda() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Double getPercentualDescontoGeral() { return percentualDescontoGeral; }
    public void setPercentualDescontoGeral(Double percentualDescontoGeral) { this.percentualDescontoGeral = percentualDescontoGeral != null ? percentualDescontoGeral : 0.0; }

    public Double getValorDescontoGeral() { return valorDescontoGeral; }
    public void setValorDescontoGeral(Double valorDescontoGeral) { this.valorDescontoGeral = valorDescontoGeral != null ? valorDescontoGeral : 0.0; }

    public LocalDate getDataVencimentoRestante() { return dataVencimentoRestante; }
    public void setDataVencimentoRestante(LocalDate dataVencimentoRestante) { this.dataVencimentoRestante = dataVencimentoRestante; }

    public List<VendaItem> getItens() { return itens; }
    public void setItens(List<VendaItem> itens) { this.itens = itens != null ? itens : new ArrayList<>(); }

    public List<Pagamento> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Pagamento> pagamentos) { this.pagamentos = pagamentos != null ? pagamentos : new ArrayList<>(); }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getTelefoneCliente() { return telefoneCliente; }
    public void setTelefoneCliente(String telefoneCliente) { this.telefoneCliente = telefoneCliente; }

    public String getEnderecoCliente() { return enderecoCliente; }
    public void setEnderecoCliente(String enderecoCliente) { this.enderecoCliente = enderecoCliente; }

    public Double getValorBruto() { return valorBruto; }
    public void setValorBruto(Double valorBruto) { this.valorBruto = valorBruto; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public Double getValorPago() { return valorPago; }
    public void setValorPago(Double valorPago) { this.valorPago = valorPago; }

    public Double getSaldoDevedor() { return saldoDevedor; }
    public void setSaldoDevedor(Double saldoDevedor) { this.saldoDevedor = saldoDevedor; }
}
