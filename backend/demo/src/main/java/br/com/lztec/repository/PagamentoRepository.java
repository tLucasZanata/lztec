package br.com.lztec.repository;

import br.com.lztec.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    List<Pagamento> findByVendaId(Integer vendaId);

    void deleteByVendaId(Integer vendaId);
}
