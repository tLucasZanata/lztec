package br.com.lztec.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;          // nome do usuário logado

    @Column(nullable = false)
    private String acao;             // CRIAR, EDITAR, EXCLUIR, ALTERAR_STATUS, LOGIN, LOGIN_FALHOU

    @Column(nullable = false)
    private String entidade;         // CLIENTE, PRODUTO, LANÇAMENTO, USUARIO, etc.

    @Column(name = "entidade_id")
    private String entidadeId;       // ID do registro afetado

    @Column(columnDefinition = "TEXT")
    private String detalhes;         // JSON com campos antes/depois

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String usuario, String acao, String entidade, String entidadeId, String detalhes) {
        this.usuario    = usuario;
        this.acao       = acao;
        this.entidade   = entidade;
        this.entidadeId = entidadeId;
        this.detalhes   = detalhes;
        this.dataHora   = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }
    public String getEntidadeId() { return entidadeId; }
    public void setEntidadeId(String entidadeId) { this.entidadeId = entidadeId; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}