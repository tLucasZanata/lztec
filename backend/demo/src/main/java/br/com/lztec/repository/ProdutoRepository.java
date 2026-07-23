package br.com.lztec.repository;

import br.com.lztec.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Busca o maior número de código cadastrado para gerar o próximo
    // Formato do código: PRD-00001
    @Query("SELECT MAX(CAST(SUBSTRING(p.codigo, 5) AS integer)) FROM Produto p WHERE p.codigo LIKE 'PRD-%'")
    Optional<Integer> findMaxCodigoNumero();
}