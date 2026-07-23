package br.com.lztec.controllers;

// Uma linha de pagamento no corpo da requisição de recebimento.
public class PagamentoLinha {
    private String formaPagamento;
    private Double valor;
    private Integer numeroParcelas;

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Integer getNumeroParcelas() { return numeroParcelas; }
    public void setNumeroParcelas(Integer numeroParcelas) { this.numeroParcelas = numeroParcelas; }
}
