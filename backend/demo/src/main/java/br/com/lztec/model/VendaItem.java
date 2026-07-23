package br.com.lztec.model;

import jakarta.persistence.*;

// Um item (produto + quantidade) dentro de uma Venda.
@Entity
@Table(name = "venda_item")
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "venda_id", nullable = false)
    private Integer vendaId;

    @Column(name = "produto_id", nullable = false)
    private Integer produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    // Preço unitário aplicado no momento da venda: usa o valor promocional
    // do produto se houver, senão o valor de venda normal. Fica "congelado"
    // aqui para não mudar o total de vendas antigas se o preço do produto mudar depois.
    @Column(name = "valor_unitario", nullable = false)
    private Double valorUnitario;

    // Desconto aplicado a este item (opcional). valorDesconto é o valor em
    // R$ abatido do subtotal do item (quantidade x valorUnitario);
    // percentualDesconto é o mesmo desconto expresso em %, mantido em sincronia
    // pelo backend a partir do valorDesconto recebido.
    @Column(name = "percentual_desconto")
    private Double percentualDesconto = 0.0;

    @Column(name = "valor_desconto")
    private Double valorDesconto = 0.0;

    // Apenas para exibição no frontend (não é uma coluna do banco)
    @Transient
    private String nomeProduto;

    public VendaItem() {}

    public VendaItem(Integer vendaId, Integer produtoId, Integer quantidade, Double valorUnitario) {
        this.vendaId = vendaId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    public Integer getProdutoId() { return produtoId; }
    public void setProdutoId(Integer produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(Double valorUnitario) { this.valorUnitario = valorUnitario; }

    public Double getPercentualDesconto() { return percentualDesconto; }
    public void setPercentualDesconto(Double percentualDesconto) { this.percentualDesconto = percentualDesconto != null ? percentualDesconto : 0.0; }

    public Double getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(Double valorDesconto) { this.valorDesconto = valorDesconto != null ? valorDesconto : 0.0; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    // Subtotal bruto do item, antes do desconto (quantidade x valorUnitario)
    @Transient
    public Double getValorBrutoItem() {
        if (valorUnitario == null || quantidade == null) return 0.0;
        return valorUnitario * quantidade;
    }

    // Subtotal líquido do item, já descontado
    @Transient
    public Double getValorTotalItem() {
        double bruto = getValorBrutoItem();
        double desc  = valorDesconto != null ? valorDesconto : 0.0;
        return Math.max(0.0, bruto - desc);
    }
}
