package br.com.lztec.config;

import br.com.lztec.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carrega usuários do banco de dados para autenticação.
 * O Spring Security chama este serviço a cada requisição com Basic Auth.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public DatabaseUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByNome(username)
                .map(u -> User.builder()
                        .username(u.getNome())
                        .password(u.getSenha())   // já é BCrypt no banco
                        .roles(u.getRole())        // ADMIN ou OPERADOR → ROLE_ADMIN / ROLE_OPERADOR
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
