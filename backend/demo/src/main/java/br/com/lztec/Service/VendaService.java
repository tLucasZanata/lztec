package br.com.lztec.Service;

import br.com.lztec.controllers.PagamentoLinha;
import br.com.lztec.model.Venda;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VendaService {
    List<Venda> findAll();
    Optional<Venda> findById(Integer id);
    Venda save(Venda venda);
    Venda update(Integer id, Venda vendaDetails);
    void delete(Integer id);
    Venda registrarPagamentos(Integer id, List<PagamentoLinha> pagamentos, LocalDate dataVencimentoRestante);
    Venda editarPagamento(Integer vendaId, Integer pagamentoId, PagamentoLinha novosDados, LocalDate dataVencimentoRestante);
    Venda excluirPagamento(Integer vendaId, Integer pagamentoId);
}
