package br.com.lztec.controllers;

import br.com.lztec.model.Produto;
import br.com.lztec.Service.ProdutoService;
import br.com.lztec.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final AuditService auditService;

    public ProdutoController(ProdutoService produtoService, AuditService auditService) {
        this.produtoService = produtoService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Integer id) {
        return produtoService.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@RequestBody Produto obj) {
        Produto novo = produtoService.save(obj);
        auditService.registrarCriacao("PRODUTO", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Integer id, @RequestBody Produto detalhes) {
        Produto antes = produtoService.findById(id).orElse(null);
        Produto atualizado = produtoService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("PRODUTO", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        Produto antes = produtoService.findById(id).orElse(null);
        produtoService.delete(id);
        auditService.registrarExclusao("PRODUTO", id, antes);
        return ResponseEntity.noContent().build();
    }
}
