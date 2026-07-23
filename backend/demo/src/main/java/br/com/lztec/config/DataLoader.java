package br.com.lztec.config;

import br.com.lztec.model.Usuario;
import br.com.lztec.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o usuário admin padrão na primeira execução se não existir nenhum ADMIN.
 * Login: admin / senha123
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean temAdmin = usuarioRepository.findAll().stream()
                .anyMatch(u -> "ADMIN".equals(u.getRole()));

        if (!temAdmin) {
            Usuario admin = new Usuario();
            admin.setNome("admin");
            admin.setEmail("admin@lztec.com");
            admin.setSenha(passwordEncoder.encode("senha123"));
            admin.setRole("ADMIN");
            usuarioRepository.save(admin);
            System.out.println(">>> Admin padrão criado: admin / senha123");
        }
    }
}
