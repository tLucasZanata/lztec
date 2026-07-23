package br.com.lztec.Service.impl;

import br.com.lztec.Service.impl.ProdutoServiceImpl;
import br.com.lztec.model.Produto;
import br.com.lztec.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProdutoServiceImpl.
 *
 * O ProdutoRepository é mockado, então nenhum banco de dados real é usado.
 * O foco aqui é a regra de negócio: geração automática do código (PRD-00001,
 * PRD-00002...), valor padrão de quantidade e comportamento de atualização parcial.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoServiceImpl - Testes Unitários")
class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoServiceImpl service;

    private Produto produtoExistente;

    @BeforeEach
    void setUp() {
        produtoExistente = new Produto("PRD-00003", "Ração Premium", "Ração para cães adultos", 89.90, 50);
        produtoExistente.setId(3);
    }

    // ════════════════════════════════════════════════════════════════════
    // save() — geração de código
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save() - geração de código")
    class GeracaoDeCodigo {

        @Test
        @DisplayName("Deve gerar PRD-00001 quando não há nenhum produto cadastrado ainda")
        void deveGerarPrimeiroCodigoQuandoBancoVazio() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.empty());
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto novo = new Produto(null, "Coleira", "Coleira ajustável", 35.0, 10);
            Produto salvo = service.save(novo);

            assertThat(salvo.getCodigo()).isEqualTo("PRD-00001");
        }

        @Test
        @DisplayName("Deve gerar o próximo código sequencial a partir do maior existente")
        void deveGerarProximoCodigoSequencial() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.of(7));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto novo = new Produto(null, "Brinquedo", "Bolinha de borracha", 12.50, 30);
            Produto salvo = service.save(novo);

            assertThat(salvo.getCodigo()).isEqualTo("PRD-00008");
        }

        @Test
        @DisplayName("Deve ignorar qualquer código enviado pelo cliente e gerar o seu próprio")
        void deveIgnorarCodigoEnviadoPeloCliente() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.of(1));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Tentativa indevida de definir um código manualmente
            Produto novo = new Produto("CODIGO-FALSO", "Shampoo", "Shampoo neutro", 22.0, 15);
            Produto salvo = service.save(novo);

            assertThat(salvo.getCodigo()).isEqualTo("PRD-00002");
        }

        @Test
        @DisplayName("Deve formatar o código sempre com 5 dígitos preenchidos com zero")
        void deveFormatarCodigoComCincoDigitos() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.of(99));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto novo = new Produto(null, "Areia higiênica", "Saco de 4kg", 18.0, 40);
            Produto salvo = service.save(novo);

            assertThat(salvo.getCodigo()).isEqualTo("PRD-00100");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // save() — quantidade padrão
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save() - quantidade padrão")
    class QuantidadePadrao {

        @Test
        @DisplayName("Deve salvar com quantidade 0 quando o construtor recebe null")
        void deveSalvarComQuantidadeZeroQuandoConstructorRecebeNull() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.empty());
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // O próprio model Produto já normaliza null para 0 no construtor/setter,
            // então este teste confirma que esse contrato é respeitado de ponta a ponta.
            Produto novo = new Produto(null, "Antipulgas", "Aplicação mensal", 45.0, null);
            Produto salvo = service.save(novo);

            assertThat(salvo.getQuantidade()).isZero();
        }

        @Test
        @DisplayName("Deve preservar a quantidade informada quando enviada")
        void devePreservarQuantidadeInformada() {
            when(produtoRepository.findMaxCodigoNumero()).thenReturn(Optional.empty());
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto novo = new Produto(null, "Petisco", "Pacote 200g", 9.90, 200);
            Produto salvo = service.save(novo);

            assertThat(salvo.getQuantidade()).isEqualTo(200);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar nome, descrição e valor de venda")
        void deveAtualizarCamposBasicos() {
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto detalhes = new Produto(null, "Ração Super Premium", "Nova fórmula", 99.90, null);
            Produto atualizado = service.update(3, detalhes);

            assertThat(atualizado.getNome()).isEqualTo("Ração Super Premium");
            assertThat(atualizado.getDescricao()).isEqualTo("Nova fórmula");
            assertThat(atualizado.getValorVenda()).isEqualTo(99.90);
        }

        @Test
        @DisplayName("Não deve alterar o código do produto na edição")
        void naoDeveAlterarCodigoNaEdicao() {
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto detalhes = new Produto("TENTATIVA-DE-MUDAR", "Nome novo", "Desc nova", 50.0, null);
            Produto atualizado = service.update(3, detalhes);

            // O código original (PRD-00003) deve permanecer intacto
            assertThat(atualizado.getCodigo()).isEqualTo("PRD-00003");
        }

        @Test
        @DisplayName("Deve atualizar a quantidade quando informada (ajuste manual de estoque)")
        void deveAtualizarQuantidadeQuandoInformada() {
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto detalhes = new Produto(null, "Ração Premium", "Ração para cães adultos", 89.90, 75);
            Produto atualizado = service.update(3, detalhes);

            assertThat(atualizado.getQuantidade()).isEqualTo(75);
        }

        @Test
        @DisplayName("ATENÇÃO: como o model Produto nunca retorna quantidade null, "
                + "o update() sempre sobrescreve o estoque mesmo quando o campo não é enviado")
        void quantidadeSempreSobrescritaPorLimitacaoDoModel() {
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Mesmo passando null no construtor, Produto.setQuantidade() normaliza para 0.
            // Por isso o "if (produtoDetails.getQuantidade() != null)" do service nunca
            // bloqueia a sobrescrita — este teste documenta esse comportamento real,
            // que pode ser uma armadilha: editar um produto sem informar quantidade
            // zera o estoque ao invés de preservar o valor atual (50).
            Produto detalhes = new Produto(null, "Ração Premium", "Ração para cães adultos", 89.90, null);
            Produto atualizado = service.update(3, detalhes);

            assertThat(atualizado.getQuantidade()).isZero();
        }

        @Test
        @DisplayName("Deve atualizar a imagemUrl na edição (BUGFIX: antes a imagem nunca era salva)")
        void deveAtualizarImagemUrlNaEdicao() {
            produtoExistente.setImagemUrl("https://cloudinary.com/imagem-antiga.jpg");
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));
            when(produtoRepository.save(any(Produto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Produto detalhes = new Produto(null, "Ração Premium", "Ração para cães adultos", 89.90, null);
            detalhes.setImagemUrl("https://cloudinary.com/imagem-nova.jpg");
            Produto atualizado = service.update(3, detalhes);

            assertThat(atualizado.getImagemUrl()).isEqualTo("https://cloudinary.com/imagem-nova.jpg");
        }

        @Test
        @DisplayName("Deve retornar null quando o produto não existe")
        void deveRetornarNullQuandoProdutoNaoExiste() {
            when(produtoRepository.findById(999)).thenReturn(Optional.empty());

            Produto resultado = service.update(999, new Produto());

            assertThat(resultado).isNull();
            verify(produtoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // findAll() / findById() / delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Consultas e exclusão")
    class ConsultasEExclusao {

        @Test
        @DisplayName("findById deve retornar o produto quando existe")
        void findByIdDeveRetornarProdutoExistente() {
            when(produtoRepository.findById(3)).thenReturn(Optional.of(produtoExistente));

            Optional<Produto> resultado = service.findById(3);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("Ração Premium");
        }

        @Test
        @DisplayName("findById deve retornar Optional vazio quando não existe")
        void findByIdDeveRetornarVazioQuandoNaoExiste() {
            when(produtoRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Produto> resultado = service.findById(999);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("delete deve chamar deleteById no repositório com o id correto")
        void deleteDeveChamarRepositorioComIdCorreto() {
            service.delete(3);

            verify(produtoRepository, times(1)).deleteById(3);
        }
    }
}