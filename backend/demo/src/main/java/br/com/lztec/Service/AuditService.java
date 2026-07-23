package br.com.lztec.Service;

import br.com.lztec.model.AuditLog;
import br.com.lztec.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

// REQUISITO BANCA: auditoria, registro e controle de logs do sistema
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public String usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonimo";
        return auth.getName();
    }

    public void registrar(String acao, String entidade, String entidadeId, String detalhes) {
        try {
            auditLogRepository.save(new AuditLog(usuarioAtual(), acao, entidade, entidadeId, detalhes));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }

    public void registrarCriacao(String entidade, Object id, Object objeto) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("acao", "CRIAR");
            d.put("dados", objeto);
            auditLogRepository.save(new AuditLog(usuarioAtual(), "CRIAR", entidade, String.valueOf(id), objectMapper.writeValueAsString(d)));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }

    public void registrarEdicao(String entidade, Object id, Object antes, Object depois) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("acao", "EDITAR");
            d.put("antes", antes);
            d.put("depois", depois);
            auditLogRepository.save(new AuditLog(usuarioAtual(), "EDITAR", entidade, String.valueOf(id), objectMapper.writeValueAsString(d)));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }

    public void registrarExclusao(String entidade, Object id, Object objeto) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("acao", "EXCLUIR");
            d.put("dado_excluido", objeto);
            auditLogRepository.save(new AuditLog(usuarioAtual(), "EXCLUIR", entidade, String.valueOf(id), objectMapper.writeValueAsString(d)));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }

    public void registrarAlteracaoStatus(String entidade, Object id, String antes, String depois) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("acao", "ALTERAR_STATUS");
            d.put("status_antes", antes);
            d.put("status_depois", depois);
            auditLogRepository.save(new AuditLog(usuarioAtual(), "ALTERAR_STATUS", entidade, String.valueOf(id), objectMapper.writeValueAsString(d)));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }

    public void registrarLogin(String nomeUsuario, boolean sucesso) {
        try {
            String acao = sucesso ? "LOGIN" : "LOGIN_FALHOU";
            String det  = sucesso ? "{\"resultado\":\"sucesso\"}" : "{\"resultado\":\"falha\"}";
            auditLogRepository.save(new AuditLog(nomeUsuario, acao, "USUARIO", null, det));
        } catch (Exception e) {
            System.err.println("Erro auditoria: " + e.getMessage());
        }
    }
}