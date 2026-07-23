package br.com.lztec.Service.impl;

import br.com.lztec.Service.VendaService;
import br.com.lztec.controllers.PagamentoLinha;
import br.com.lztec.model.Pagamento;
import br.com.lztec.model.Produto;
import br.com.lztec.model.Venda;
import br.com.lztec.model.VendaItem;
import br.com.lztec.repository.ClienteRepository;
import br.com.lztec.repository.PagamentoRepository;
import br.com.lztec.repository.ProdutoRepository;
import br.com.lztec.repository.VendaItemRepository;
import br.com.lztec.repository.VendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VendaServiceImpl implements VendaService {

    private static final List<String> FORMAS_VALIDAS = List.of("DINHEIRO", "PIX", "CARTAO_DEBITO", "CARTAO_CREDITO");
    private static final double TOLERANCIA = 0.01;

    private final VendaRepository vendaRepository;
    private final VendaItemRepository vendaItemRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoRepository pagamentoRepository;

    public VendaServiceImpl(VendaRepository vendaRepository,
                             VendaItemRepository vendaItemRepository,
                             ProdutoRepository produtoRepository,
                             ClienteRepository clienteRepository,
                             PagamentoRepository pagamentoRepository) {
        this.vendaRepository = vendaRepository;
        this.vendaItemRepository = vendaItemRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    // Preenche o nome do cliente, itens (com nome do produto), pagamentos já
    // recebidos e valorBruto/valorTotal/valorPago/saldoDevedor de uma venda,
    // para exibição no frontend.
    private void preencherDadosExibicao(Venda venda) {
        clienteRepository.findById(venda.getClienteId()).ifPresent(c -> {
            venda.setNomeCliente(c.getNome());
            venda.setTelefoneCliente(c.getContato());
            venda.setEnderecoCliente(c.getEndereco());
        });

        List<VendaItem> itens = vendaItemRepository.findByVendaId(venda.getId());
        double bruto = 0.0;
        for (VendaItem item : itens) {
            produtoRepository.findById(item.getProdutoId())
                    .ifPresent(p -> item.setNomeProduto(p.getNome()));
            bruto += item.getValorTotalItem();
        }
        venda.setItens(itens);
        venda.setValorBruto(bruto);
        double total = Math.max(0.0, bruto - venda.getValorDescontoGeral());
        venda.setValorTotal(total);

        List<Pagamento> pagamentos = pagamentoRepository.findByVendaId(venda.getId());
        double pago = pagamentos.stream().mapToDouble(Pagamento::getValor).sum();
        venda.setPagamentos(pagamentos);
        venda.setValorPago(pago);
        venda.setSaldoDevedor(Math.max(0.0, total - pago));
    }

    // O desconto (por item ou geral) usa o valor em R$ enviado pelo cliente
    // como fonte da verdade: nunca deixa passar de 0..base, e recalcula o
    // percentual a partir dele para os dois campos ficarem sempre em sincronia,
    // independente do que o front mandou calculado.
    private double aplicarDescontoClamp(double valorDescontoEnviado, double base,
                                         java.util.function.Consumer<Double> setValor,
                                         java.util.function.Consumer<Double> setPercentual) {
        double valor = Math.max(0.0, Math.min(valorDescontoEnviado, base));
        double percentual = base > 0 ? (valor / base) * 100.0 : 0.0;
        setValor.accept(valor);
        setPercentual.accept(percentual);
        return valor;
    }

    // Valida estoque e desconta a quantidade vendida de cada produto,
    // "congelando" o valor unitário usado (promocional, se houver), e
    // aplica/valida o desconto individual de cada item.
    private void descontarEstoque(List<VendaItem> itens) {
        for (VendaItem item : itens) {
            Produto p = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Produto ID " + item.getProdutoId() + " não encontrado."));
            if (p.getQuantidade() == null || p.getQuantidade() < item.getQuantidade()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Estoque insuficiente para o produto \"" + p.getNome() + "\". Disponível: "
                                + (p.getQuantidade() == null ? 0 : p.getQuantidade()));
            }
            double valorUnit = p.getValorPromocional() != null ? p.getValorPromocional() : p.getValorVenda();
            item.setValorUnitario(valorUnit);
            p.setQuantidade(p.getQuantidade() - item.getQuantidade());
            produtoRepository.save(p);

            double brutoItem = valorUnit * item.getQuantidade();
            aplicarDescontoClamp(item.getValorDesconto(), brutoItem, item::setValorDesconto, item::setPercentualDesconto);
        }
    }

    // Aplica/valida o desconto geral da venda, com base na soma (já líquida)
    // dos itens.
    private void aplicarDescontoGeral(Venda venda, List<VendaItem> itens) {
        double baseGeral = itens.stream().mapToDouble(VendaItem::getValorTotalItem).sum();
        aplicarDescontoClamp(venda.getValorDescontoGeral(), baseGeral,
                venda::setValorDescontoGeral, venda::setPercentualDescontoGeral);
    }

    // Recalcula o status (PENDENTE/PARCIAL/PAGO) de uma venda com base no
    // total já pago comparado ao valor total atual dela.
    private void recalcularStatusPagamento(Venda venda, double valorTotal) {
        double pago = pagamentoRepository.findByVendaId(venda.getId()).stream().mapToDouble(Pagamento::getValor).sum();
        if (pago >= valorTotal - TOLERANCIA) {
            venda.setStatus("PAGO");
            venda.setDataVencimentoRestante(null);
        } else if (pago > TOLERANCIA) {
            venda.setStatus("PARCIAL");
        } else {
            venda.setStatus("PENDENTE");
            venda.setDataVencimentoRestante(null);
        }
    }

    // Devolve ao estoque os produtos de uma venda (usado ao editar ou excluir)
    private void devolverEstoque(Integer vendaId) {
        List<VendaItem> itensAntigos = vendaItemRepository.findByVendaId(vendaId);
        for (VendaItem item : itensAntigos) {
            produtoRepository.findById(item.getProdutoId()).ifPresent(p -> {
                p.setQuantidade((p.getQuantidade() == null ? 0 : p.getQuantidade()) + item.getQuantidade());
                produtoRepository.save(p);
            });
        }
    }

    @Override
    public List<Venda> findAll() {
        List<Venda> vendas = vendaRepository.findAll();
        vendas.forEach(this::preencherDadosExibicao);
        return vendas;
    }

    @Override
    public Optional<Venda> findById(Integer id) {
        Optional<Venda> venda = vendaRepository.findById(id);
        venda.ifPresent(this::preencherDadosExibicao);
        return venda;
    }

    @Override
    @Transactional
    public Venda save(Venda venda) {
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A venda precisa ter pelo menos um produto.");
        }
        clienteRepository.findById(venda.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        List<VendaItem> itens = venda.getItens();
        venda.setItens(null);
        venda.setStatus("PENDENTE");
        venda.setDataVencimentoRestante(null);
        Venda salva = vendaRepository.save(venda);

        for (VendaItem item : itens) {
            item.setVendaId(salva.getId());
            item.setId(null);
        }
        descontarEstoque(itens);
        aplicarDescontoGeral(salva, itens);
        vendaItemRepository.saveAll(itens);
        salva = vendaRepository.save(salva);

        preencherDadosExibicao(salva);
        return salva;
    }

    @Override
    @Transactional
    public Venda update(Integer id, Venda vendaDetails) {
        return vendaRepository.findById(id).map(existente -> {
            // Devolve o estoque dos itens atuais antes de aplicar os novos
            devolverEstoque(id);
            vendaItemRepository.deleteByVendaId(id);

            existente.setClienteId(vendaDetails.getClienteId());
            existente.setDataVenda(vendaDetails.getDataVenda());
            existente.setObservacao(vendaDetails.getObservacao());
            existente.setValorDescontoGeral(vendaDetails.getValorDescontoGeral());

            List<VendaItem> novosItens = vendaDetails.getItens();
            if (novosItens == null || novosItens.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A venda precisa ter pelo menos um produto.");
            }
            for (VendaItem item : novosItens) {
                item.setVendaId(id);
                item.setId(null);
            }
            descontarEstoque(novosItens);
            aplicarDescontoGeral(existente, novosItens);
            vendaItemRepository.saveAll(novosItens);

            // Reavalia o status de pagamento, já que o valor total pode ter mudado
            double novoTotal = novosItens.stream().mapToDouble(VendaItem::getValorTotalItem).sum() - existente.getValorDescontoGeral();
            recalcularStatusPagamento(existente, Math.max(0.0, novoTotal));

            Venda atualizada = vendaRepository.save(existente);
            preencherDadosExibicao(atualizada);
            return atualizada;
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        devolverEstoque(id);
        vendaItemRepository.deleteByVendaId(id);
        pagamentoRepository.deleteByVendaId(id);
        vendaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Venda registrarPagamentos(Integer id, List<PagamentoLinha> linhas, LocalDate dataVencimentoRestante) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));

        if (linhas == null || linhas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe pelo menos uma forma de pagamento.");
        }

        double valorTotal = vendaItemRepository.findByVendaId(id).stream()
                .mapToDouble(VendaItem::getValorTotalItem).sum() - venda.getValorDescontoGeral();
        valorTotal = Math.max(0.0, valorTotal);

        double jaPago = pagamentoRepository.findByVendaId(id).stream().mapToDouble(Pagamento::getValor).sum();
        double somaNovosPagamentos = 0.0;

        for (PagamentoLinha linha : linhas) {
            if (linha.getFormaPagamento() == null || !FORMAS_VALIDAS.contains(linha.getFormaPagamento())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Forma de pagamento inválida. Use: DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO.");
            }
            if (linha.getValor() == null || linha.getValor() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor de cada pagamento deve ser maior que zero.");
            }
            somaNovosPagamentos += linha.getValor();
        }

        if (jaPago + somaNovosPagamentos > valorTotal + TOLERANCIA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O total informado (" + (jaPago + somaNovosPagamentos) + ") é maior que o valor da venda (" + valorTotal + ").");
        }

        boolean vaiFicarParcial = (jaPago + somaNovosPagamentos) < valorTotal - TOLERANCIA;
        if (vaiFicarParcial && dataVencimentoRestante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe a data prevista para o pagamento do restante.");
        }

        for (PagamentoLinha linha : linhas) {
            Pagamento pagamento = new Pagamento();
            pagamento.setVendaId(id);
            pagamento.setFormaPagamento(linha.getFormaPagamento());
            pagamento.setValor(linha.getValor());
            pagamento.setNumeroParcelas(linha.getNumeroParcelas());
            pagamento.setDataPagamento(LocalDate.now());
            pagamentoRepository.save(pagamento);
        }

        recalcularStatusPagamento(venda, valorTotal);
        if (vaiFicarParcial) {
            venda.setDataVencimentoRestante(dataVencimentoRestante);
        }

        Venda salva = vendaRepository.save(venda);
        preencherDadosExibicao(salva);
        return salva;
    }

    // Calcula o valor total atual da venda (soma dos itens já com desconto
    // individual, menos o desconto geral) — usado pra validar pagamentos.
    private double calcularValorTotalAtual(Integer vendaId, Venda venda) {
        double totalItens = vendaItemRepository.findByVendaId(vendaId).stream()
                .mapToDouble(VendaItem::getValorTotalItem).sum();
        return Math.max(0.0, totalItens - venda.getValorDescontoGeral());
    }

    @Override
    @Transactional
    public Venda editarPagamento(Integer vendaId, Integer pagamentoId, PagamentoLinha novosDados, LocalDate dataVencimentoRestante) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        if (!pagamento.getVendaId().equals(vendaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esse pagamento não pertence a essa venda.");
        }
        if (novosDados.getFormaPagamento() == null || !FORMAS_VALIDAS.contains(novosDados.getFormaPagamento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Forma de pagamento inválida. Use: DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO.");
        }
        if (novosDados.getValor() == null || novosDados.getValor() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor do pagamento deve ser maior que zero.");
        }

        double valorTotal = calcularValorTotalAtual(vendaId, venda);
        double outrosPagamentos = pagamentoRepository.findByVendaId(vendaId).stream()
                .filter(p -> !p.getId().equals(pagamentoId))
                .mapToDouble(Pagamento::getValor).sum();

        if (outrosPagamentos + novosDados.getValor() > valorTotal + TOLERANCIA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esse valor deixaria o total pago maior que o valor da venda.");
        }

        pagamento.setFormaPagamento(novosDados.getFormaPagamento());
        pagamento.setValor(novosDados.getValor());
        pagamento.setNumeroParcelas(novosDados.getNumeroParcelas());
        pagamentoRepository.save(pagamento);

        boolean vaiFicarParcial = (outrosPagamentos + novosDados.getValor()) < valorTotal - TOLERANCIA;
        if (vaiFicarParcial && dataVencimentoRestante == null && venda.getDataVencimentoRestante() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Como a venda vai ficar parcial, informe a data prevista para o restante.");
        }

        recalcularStatusPagamento(venda, valorTotal);
        if (vaiFicarParcial && dataVencimentoRestante != null) {
            venda.setDataVencimentoRestante(dataVencimentoRestante);
        }
        Venda salva = vendaRepository.save(venda);
        preencherDadosExibicao(salva);
        return salva;
    }

    @Override
    @Transactional
    public Venda excluirPagamento(Integer vendaId, Integer pagamentoId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        if (!pagamento.getVendaId().equals(vendaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esse pagamento não pertence a essa venda.");
        }
        pagamentoRepository.deleteById(pagamentoId);

        double valorTotal = calcularValorTotalAtual(vendaId, venda);
        recalcularStatusPagamento(venda, valorTotal);
        Venda salva = vendaRepository.save(venda);
        preencherDadosExibicao(salva);
        return salva;
    }
}
