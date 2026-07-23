package br.com.lztec.repository;

import br.com.lztec.model.VendaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendaItemRepository extends JpaRepository<VendaItem, Integer> {

    List<VendaItem> findByVendaId(Integer vendaId);

    void deleteByVendaId(Integer vendaId);
}
