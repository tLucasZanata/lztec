package br.com.lztec.Service.impl;

import br.com.lztec.model.Produto;
import br.com.lztec.repository.ProdutoRepository;
import br.com.lztec.Service.ProdutoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // Gera o proximo codigo no formato PRD-00001
    private String gerarProximoCodigo() {
        int proximo = produtoRepository.findMaxCodigoNumero()
                .map(max -> max + 1)
                .orElse(1);
        return String.format("PRD-%05d", proximo);
    }

    @Override
    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    @Override
    public Optional<Produto> findById(Integer id) {
        return produtoRepository.findById(id);
    }

    @Override
    public Produto save(Produto produto) {
        // Codigo sempre gerado pelo sistema - ignora qualquer valor enviado pelo cliente
        produto.setCodigo(gerarProximoCodigo());
        if (produto.getQuantidade() == null) {
            produto.setQuantidade(0);
        }
        return produtoRepository.save(produto);
    }

    @Override
    public Produto update(Integer id, Produto produtoDetails) {
        return produtoRepository.findById(id).map(existente -> {
            existente.setNome(produtoDetails.getNome());
            existente.setDescricao(produtoDetails.getDescricao());
            existente.setValorVenda(produtoDetails.getValorVenda());
            existente.setValorPromocional(produtoDetails.getValorPromocional());
            // BUGFIX: imagemUrl nao era copiada na edicao - imagem trocada no
            // formulario nunca era salva. Codigo nao e alterado na edicao.
            existente.setImagemUrl(produtoDetails.getImagemUrl());
            // Quantidade e editavel (ajuste manual de estoque)
            if (produtoDetails.getQuantidade() != null) {
                existente.setQuantidade(produtoDetails.getQuantidade());
            }
            return produtoRepository.save(existente);
        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        produtoRepository.deleteById(id);
    }
}