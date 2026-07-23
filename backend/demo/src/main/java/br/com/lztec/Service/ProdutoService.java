package br.com.lztec.Service;

import br.com.lztec.model.Produto;
import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    List<Produto> findAll();
    Optional<Produto> findById(Integer id);
    Produto save(Produto produto);
    Produto update(Integer id, Produto produto);
    void delete(Integer id);
}