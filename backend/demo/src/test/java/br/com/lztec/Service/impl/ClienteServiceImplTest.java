package br.com.lztec.Service.impl;

import br.com.lztec.Service.impl.ClienteServiceImpl;
import br.com.lztec.model.Cliente;
import br.com.lztec.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ClienteServiceImpl.
 *
 * Este Service é um CRUD simples, sem regras de negócio complexas como
 * estoque ou status. O foco aqui é garantir que o update() realmente
 * atualiza todos os campos esperados e que o comportamento de "não
 * encontrado" é tratado corretamente em update/findById.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - Testes Unitários")
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl service;

    private Cliente clienteExistente;

    @BeforeEach
    void setUp() {
        clienteExistente = new Cliente(
                "Rafael Souza", "123.456.789-00", "rafael@email.com",
                "(44) 99999-0000", "Rua A, 100", "CAD-001", null
        );
        clienteExistente.setId(7);
    }

    // ════════════════════════════════════════════════════════════════════
    // save()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Deve salvar um novo cliente delegando diretamente ao repositório")
        void deveSalvarNovoCliente() {
            Cliente novo = new Cliente("Maria Lima", "987.654.321-00", "maria@email.com",
                    "(44) 98888-1111", "Rua B, 200", null, null);

            when(clienteRepository.save(novo)).thenReturn(novo);

            Cliente salvo = service.save(novo);

            assertThat(salvo.getNome()).isEqualTo("Maria Lima");
            verify(clienteRepository, times(1)).save(novo);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar todos os campos do cliente existente")
        void deveAtualizarTodosOsCampos() {
            when(clienteRepository.findById(7)).thenReturn(Optional.of(clienteExistente));
            when(clienteRepository.save(any(Cliente.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Cliente detalhes = new Cliente(
                    "Rafael Souza Jr.", "111.222.333-44", "rafael.jr@email.com",
                    "(44) 97777-2222", "Rua Nova, 500", "CAD-099", "Sala 12"
            );

            Cliente atualizado = service.update(7, detalhes);

            assertThat(atualizado.getNome()).isEqualTo("Rafael Souza Jr.");
            assertThat(atualizado.getCpfCnpj()).isEqualTo("111.222.333-44");
            assertThat(atualizado.getEmail()).isEqualTo("rafael.jr@email.com");
            assertThat(atualizado.getContato()).isEqualTo("(44) 97777-2222");
            assertThat(atualizado.getEndereco()).isEqualTo("Rua Nova, 500");
            assertThat(atualizado.getCadPro()).isEqualTo("CAD-099");
            assertThat(atualizado.getEnderecoAdicional()).isEqualTo("Sala 12");
        }

        @Test
        @DisplayName("Deve manter o mesmo ID do cliente original após a atualização")
        void deveManterIdOriginal() {
            when(clienteRepository.findById(7)).thenReturn(Optional.of(clienteExistente));
            when(clienteRepository.save(any(Cliente.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Cliente detalhes = new Cliente("Nome Alterado", null, null, null, null, null, null);

            Cliente atualizado = service.update(7, detalhes);

            assertThat(atualizado.getId()).isEqualTo(7);
        }

        @Test
        @DisplayName("Deve retornar null quando o cliente não existe")
        void deveRetornarNullQuandoNaoExiste() {
            when(clienteRepository.findById(999)).thenReturn(Optional.empty());

            Cliente resultado = service.update(999, new Cliente());

            assertThat(resultado).isNull();
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve sobrescrever campos com null quando os detalhes não trazem valor")
        void deveSobrescreverComNullQuandoDetalhesVazios() {
            // Este teste documenta o comportamento real: diferente do UsuarioServiceImpl,
            // o update() do Cliente NÃO verifica se os campos são nulos antes de aplicar —
            // ele sempre copia todos os campos de clienteDetails, mesmo que venham vazios.
            when(clienteRepository.findById(7)).thenReturn(Optional.of(clienteExistente));
            when(clienteRepository.save(any(Cliente.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Cliente detalhesVazios = new Cliente(); // todos os campos nulos

            Cliente atualizado = service.update(7, detalhesVazios);

            assertThat(atualizado.getNome()).isNull();
            assertThat(atualizado.getEmail()).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // findAll() / findById() / delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Consultas e exclusão")
    class ConsultasEExclusao {

        @Test
        @DisplayName("findAll deve retornar todos os clientes cadastrados")
        void findAllDeveRetornarTodos() {
            Cliente outro = new Cliente("Outro Cliente", null, null, null, null, null, null);
            when(clienteRepository.findAll()).thenReturn(List.of(clienteExistente, outro));

            List<Cliente> resultado = service.findAll();

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("findById deve retornar o cliente quando existe")
        void findByIdDeveRetornarClienteExistente() {
            when(clienteRepository.findById(7)).thenReturn(Optional.of(clienteExistente));

            Optional<Cliente> resultado = service.findById(7);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("Rafael Souza");
        }

        @Test
        @DisplayName("findById deve retornar Optional vazio quando não existe")
        void findByIdDeveRetornarVazioQuandoNaoExiste() {
            when(clienteRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Cliente> resultado = service.findById(999);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("delete deve chamar deleteById no repositório com o id correto")
        void deleteDeveChamarRepositorioComIdCorreto() {
            service.delete(7);

            verify(clienteRepository, times(1)).deleteById(7);
        }
    }
}
