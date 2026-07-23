package br.com.lztec.Service;

import br.com.lztec.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Integer id);
    Usuario save(Usuario usuario, PasswordEncoder encoder);
    Usuario update(Integer id, Usuario usuarioDetails);
    void delete(Integer id);
    boolean alterarSenha(String username, String senhaAtual, String novaSenha, PasswordEncoder encoder);
}
