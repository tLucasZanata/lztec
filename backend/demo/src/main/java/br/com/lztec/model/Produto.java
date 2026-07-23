package br.com.lztec.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Gerado automaticamente pelo sistema: PRD-00001, PRD-00002...
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor_venda", nullable = false)
    private Double valorVenda;

    // Preço promocional, opcional. Quando preenchido, é o valor usado na venda.
    @Column(name = "valor_promocional")
    private Double valorPromocional;

    // Quantidade disponivel em estoque
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 0;

    // URL da imagem do produto, hospedada na nuvem (Cloudinary)
    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    public Produto() {}

    public Produto(String codigo, String nome, String descricao, Double valorVenda, Integer quantidade) {
        this.codigo     = codigo;
        this.nome       = nome;
        this.descricao  = descricao;
        this.valorVenda = valorVenda;
        this.quantidade = quantidade != null ? quantidade : 0;
    }

    public Integer getId()                   { return id; }
    public void setId(Integer id)            { this.id = id; }

    public String getCodigo()                { return codigo; }
    public void setCodigo(String codigo)     { this.codigo = codigo; }

    public String getNome()                  { return nome; }
    public void setNome(String nome)         { this.nome = nome; }

    public String getDescricao()             { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getValorVenda()            { return valorVenda; }
    public void setValorVenda(Double v)      { this.valorVenda = v; }

    public Double getValorPromocional()               { return valorPromocional; }
    public void setValorPromocional(Double valorPromocional) { this.valorPromocional = valorPromocional; }

    public Integer getQuantidade()           { return quantidade; }
    public void setQuantidade(Integer q)     { this.quantidade = q != null ? q : 0; }

    public String getImagemUrl()             { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
}