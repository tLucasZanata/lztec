package br.com.lztec.repository;

import br.com.lztec.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Monta a busca de auditoria com filtros opcionais, adicionando cada
 * condicao na query apenas quando o filtro correspondente foi informado.
 * Veja o comentario em AuditLogRepository para o motivo desta abordagem.
 */
public class AuditLogSpecifications {

    public static Specification<AuditLog> comFiltros(
            String usuario, String acao, String entidade,
            LocalDateTime inicio, LocalDateTime fim) {

        return (root, query, cb) -> {
            var predicado = cb.conjunction();

            if (usuario != null && !usuario.isBlank()) {
                predicado = cb.and(predicado,
                        cb.like(cb.lower(root.get("usuario")), "%" + usuario.toLowerCase() + "%"));
            }
            if (acao != null && !acao.isBlank()) {
                predicado = cb.and(predicado, cb.equal(root.get("acao"), acao));
            }
            if (entidade != null && !entidade.isBlank()) {
                predicado = cb.and(predicado, cb.equal(root.get("entidade"), entidade));
            }
            if (inicio != null) {
                predicado = cb.and(predicado, cb.greaterThanOrEqualTo(root.get("dataHora"), inicio));
            }
            if (fim != null) {
                predicado = cb.and(predicado, cb.lessThanOrEqualTo(root.get("dataHora"), fim));
            }
            return predicado;
        };
    }
}