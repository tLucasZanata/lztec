package br.com.lztec.controllers;

import br.com.lztec.model.Venda;
import br.com.lztec.Service.VendaService;
import br.com.lztec.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService vendaService;
    private final AuditService auditService;

    public VendaController(VendaService vendaService, AuditService auditService) {
        this.vendaService = vendaService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Venda>> listarTodas() {
        return ResponseEntity.ok(vendaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Integer id) {
        return vendaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Venda> criarVenda(@RequestBody Venda venda) {
        Venda nova = vendaService.save(venda);
        auditService.registrarCriacao("VENDA", nova.getId(), nova);
        return ResponseEntity.status(HttpStatus.CREATED).body(nova);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venda> atualizarVenda(@PathVariable Integer id, @RequestBody Venda detalhes) {
        Venda atualizada = vendaService.update(id, detalhes);
        if (atualizada == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("VENDA", id, null, atualizada);
        return ResponseEntity.ok(atualizada);
    }

    @PatchMapping("/{id}/recebimento")
    public ResponseEntity<Venda> confirmarRecebimento(@PathVariable Integer id, @RequestBody RecebimentoRequest req) {
        Venda atualizada = vendaService.registrarPagamentos(id, req.getPagamentos(), req.getDataVencimentoRestante());
        auditService.registrar("RECEBIMENTO", "VENDA", String.valueOf(id),
                "Pagamento registrado (" + (req.getPagamentos() != null ? req.getPagamentos().size() : 0) + " forma(s))");
        return ResponseEntity.ok(atualizada);
    }

    @PutMapping("/{id}/pagamentos/{pagamentoId}")
    public ResponseEntity<Venda> editarPagamento(@PathVariable Integer id, @PathVariable Integer pagamentoId,
                                                  @RequestBody RecebimentoRequest req) {
        PagamentoLinha dados = (req.getPagamentos() != null && !req.getPagamentos().isEmpty()) ? req.getPagamentos().get(0) : null;
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        Venda atualizada = vendaService.editarPagamento(id, pagamentoId, dados, req.getDataVencimentoRestante());
        auditService.registrar("EDICAO_PAGAMENTO", "VENDA", String.valueOf(id), "Pagamento #" + pagamentoId + " editado");
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}/pagamentos/{pagamentoId}")
    public ResponseEntity<Venda> excluirPagamento(@PathVariable Integer id, @PathVariable Integer pagamentoId) {
        Venda atualizada = vendaService.excluirPagamento(id, pagamentoId);
        auditService.registrar("EXCLUSAO_PAGAMENTO", "VENDA", String.valueOf(id), "Pagamento #" + pagamentoId + " removido");
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVenda(@PathVariable Integer id) {
        vendaService.delete(id);
        auditService.registrarExclusao("VENDA", id, null);
        return ResponseEntity.noContent().build();
    }
}
