package br.com.lztec.repository;

import br.com.lztec.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * BUGFIX: a busca de auditoria usava JPQL com "(:param IS NULL OR ...)"
 * para filtros opcionais. O driver JDBC do PostgreSQL nao consegue inferir
 * o tipo de um parametro quando ele e nulo nesse tipo de comparacao,
 * causando "ERROR: could not determine data type of parameter".
 *
 * A solucao e usar JpaSpecificationExecutor: a query e montada
 * dinamicamente em AuditLogSpecifications, adicionando cada condicao
 * SOMENTE quando o filtro correspondente nao e nulo. Assim, nenhum
 * parametro nulo e enviado ao banco nessa comparacao problematica.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findTop100ByOrderByDataHoraDesc();
}