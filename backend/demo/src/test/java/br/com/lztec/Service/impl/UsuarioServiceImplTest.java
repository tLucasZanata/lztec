package br.com.lztec.Service.impl;

import br.com.lztec.model.Usuario;
import br.com.lztec.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UsuarioServiceImpl.
 *
 * O UsuarioRepository e o PasswordEncoder são mockados. O PasswordEncoder
 * em especial é importante simular com cuidado: nos testes reais ele faz
 * hash com BCrypt, mas aqui simulamos o comportamento esperado (encode
 * transforma a senha em texto, matches compara senha em texto com hash)
 * sem precisar calcular hash de verdade — o foco é a regra de negócio do
 * service, não o algoritmo de criptografia em si.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl - Testes Unitários")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl service;

    private Usuario usuarioExistente;

    @BeforeEach
    void setUp() {
        usuarioExistente = new Usuario("lucas", "lucas@lztec.com", "$2a$10$hashFicticio", "OPERADOR");
        usuarioExistente.setId(5);
    }

    // ════════════════════════════════════════════════════════════════════
    // save() — validações de criação
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save() - validações")
    class SaveValidacoes {

        @Test
        @DisplayName("Deve criar usuário com sucesso quando todos os dados são válidos")
        void deveCriarUsuarioComSucesso() {
            Usuario novo = new Usuario("admin2", "admin2@lztec.com", "senha123", "ADMIN");

            when(usuarioRepository.existsByNome("admin2")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashGerado");
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Usuario salvo = service.save(novo, passwordEncoder);

            assertThat(salvo.getSenha()).isEqualTo("$2a$10$hashGerado");
            verify(passwordEncoder).encode("senha123");
        }

        @Test
        @DisplayName("Deve rejeitar criação quando já existe usuário com o mesmo nome")
        void deveRejeitarNomeDuplicado() {
            Usuario duplicado = new Usuario("lucas", "outro@lztec.com", "senha123", "OPERADOR");

            when(usuarioRepository.existsByNome("lucas")).thenReturn(true);

            assertThatThrownBy(() -> service.save(duplicado, passwordEncoder))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Já existe um usuário");

            verify(usuarioRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Deve rejeitar senha com menos de 6 caracteres")
        void deveRejeitarSenhaCurta() {
            Usuario senhaFraca = new Usuario("novato", "novato@lztec.com", "12345", "OPERADOR");

            when(usuarioRepository.existsByNome("novato")).thenReturn(false);

            assertThatThrownBy(() -> service.save(senhaFraca, passwordEncoder))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("pelo menos 6 caracteres");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar criação quando a senha é nula")
        void deveRejeitarSenhaNula() {
            Usuario semSenha = new Usuario("semsenha", "semsenha@lztec.com", null, "OPERADOR");

            when(usuarioRepository.existsByNome("semsenha")).thenReturn(false);

            assertThatThrownBy(() -> service.save(semSenha, passwordEncoder))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("pelo menos 6 caracteres");
        }

        @Test
        @DisplayName("Deve rejeitar role diferente de ADMIN ou OPERADOR")
        void deveRejeitarRoleInvalida() {
            Usuario roleErrada = new Usuario("hacker", "hacker@lztec.com", "senha123", "SUPERUSER");

            when(usuarioRepository.existsByNome("hacker")).thenReturn(false);

            assertThatThrownBy(() -> service.save(roleErrada, passwordEncoder))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Role inválida");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar role nula")
        void deveRejeitarRoleNula() {
            Usuario semRole = new Usuario("semrole", "semrole@lztec.com", "senha123", null);

            when(usuarioRepository.existsByNome("semrole")).thenReturn(false);

            assertThatThrownBy(() -> service.save(semRole, passwordEncoder))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Role inválida");
        }

        @Test
        @DisplayName("Deve aceitar role ADMIN")
        void deveAceitarRoleAdmin() {
            Usuario admin = new Usuario("chefe", "chefe@lztec.com", "senha123", "ADMIN");

            when(usuarioRepository.existsByNome("chefe")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Usuario salvo = service.save(admin, passwordEncoder);

            assertThat(salvo.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Deve aceitar role OPERADOR")
        void deveAceitarRoleOperador() {
            Usuario operador = new Usuario("atendente", "atendente@lztec.com", "senha123", "OPERADOR");

            when(usuarioRepository.existsByNome("atendente")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Usuario salvo = service.save(operador, passwordEncoder);

            assertThat(salvo.getRole()).isEqualTo("OPERADOR");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar apenas os campos informados (nome, email, role)")
        void deveAtualizarCamposInformados() {
            when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Usuario detalhes = new Usuario();
            detalhes.setNome("lucas.silva");
            detalhes.setEmail("lucas.silva@lztec.com");
            detalhes.setRole("ADMIN");

            Usuario atualizado = service.update(5, detalhes);

            assertThat(atualizado.getNome()).isEqualTo("lucas.silva");
            assertThat(atualizado.getEmail()).isEqualTo("lucas.silva@lztec.com");
            assertThat(atualizado.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Não deve alterar a senha através do update()")
        void naoDeveAlterarSenhaPeloUpdate() {
            when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Usuario detalhes = new Usuario();
            detalhes.setNome("lucas.novo");
            // mesmo que alguém tente injetar uma senha no DTO de update,
            // o service não possui setSenha aqui — a senha original permanece
            String senhaAntes = usuarioExistente.getSenha();

            Usuario atualizado = service.update(5, detalhes);

            assertThat(atualizado.getSenha()).isEqualTo(senhaAntes);
        }

        @Test
        @DisplayName("Não deve sobrescrever campos quando vierem nulos")
        void naoDeveSobrescreverCamposNulos() {
            when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Detalhes totalmente vazio: nome, email e role nulos
            Usuario detalhesVazio = new Usuario();

            Usuario atualizado = service.update(5, detalhesVazio);

            // Os valores originais devem permanecer intactos
            assertThat(atualizado.getNome()).isEqualTo("lucas");
            assertThat(atualizado.getEmail()).isEqualTo("lucas@lztec.com");
            assertThat(atualizado.getRole()).isEqualTo("OPERADOR");
        }

        @Test
        @DisplayName("Deve retornar null quando o usuário não existe")
        void deveRetornarNullQuandoNaoExiste() {
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

            Usuario resultado = service.update(999, new Usuario());

            assertThat(resultado).isNull();
            verify(usuarioRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // alterarSenha()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("alterarSenha()")
    class AlterarSenha {

        @Test
        @DisplayName("Deve alterar a senha quando a senha atual está correta")
        void deveAlterarSenhaComSucesso() {
            when(usuarioRepository.findByNome("lucas")).thenReturn(Optional.of(usuarioExistente));
            when(passwordEncoder.matches("senhaAntiga", usuarioExistente.getSenha())).thenReturn(true);
            when(passwordEncoder.encode("senhaNova123")).thenReturn("$2a$10$hashNovo");

            boolean resultado = service.alterarSenha("lucas", "senhaAntiga", "senhaNova123", passwordEncoder);

            assertThat(resultado).isTrue();
            verify(usuarioRepository).save(argThat(u -> u.getSenha().equals("$2a$10$hashNovo")));
        }

        @Test
        @DisplayName("Deve retornar false quando a senha atual está incorreta")
        void deveRetornarFalseQuandoSenhaAtualIncorreta() {
            when(usuarioRepository.findByNome("lucas")).thenReturn(Optional.of(usuarioExistente));
            when(passwordEncoder.matches("senhaErrada", usuarioExistente.getSenha())).thenReturn(false);

            boolean resultado = service.alterarSenha("lucas", "senhaErrada", "senhaNova123", passwordEncoder);

            assertThat(resultado).isFalse();
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar false quando o usuário não existe")
        void deveRetornarFalseQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findByNome("fantasma")).thenReturn(Optional.empty());

            boolean resultado = service.alterarSenha("fantasma", "qualquer", "novaSenha123", passwordEncoder);

            assertThat(resultado).isFalse();
            verify(passwordEncoder, never()).matches(any(), any());
            verify(usuarioRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // findAll() / findById() / delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Consultas e exclusão")
    class ConsultasEExclusao {

        @Test
        @DisplayName("findById deve retornar o usuário quando existe")
        void findByIdDeveRetornarUsuarioExistente() {
            when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioExistente));

            Optional<Usuario> resultado = service.findById(5);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("lucas");
        }

        @Test
        @DisplayName("delete deve chamar deleteById no repositório com o id correto")
        void deleteDeveChamarRepositorioComIdCorreto() {
            service.delete(5);

            verify(usuarioRepository, times(1)).deleteById(5);
        }
    }
}
