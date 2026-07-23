package br.com.lztec.controllers;

import br.com.lztec.model.Cliente;
import br.com.lztec.Service.ClienteService;
import br.com.lztec.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final AuditService   auditService;

    public ClienteController(ClienteService clienteService, AuditService auditService) {
        this.clienteService = clienteService;
        this.auditService   = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Integer id) {
        return clienteService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Cliente novo = clienteService.save(cliente);
        auditService.registrarCriacao("CLIENTE", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizarCliente(@PathVariable Integer id, @RequestBody Cliente detalhes) {
        Cliente antes = clienteService.findById(id).orElse(null);
        Cliente atualizado = clienteService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("CLIENTE", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Integer id) {
        Cliente antes = clienteService.findById(id).orElse(null);
        clienteService.delete(id);
        auditService.registrarExclusao("CLIENTE", id, antes);
        return ResponseEntity.noContent().build();
    }
}
