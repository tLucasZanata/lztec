package br.com.lztec.Service.impl;

import br.com.lztec.model.Usuario;
import br.com.lztec.repository.UsuarioRepository;
import br.com.lztec.Service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario save(Usuario usuario, PasswordEncoder encoder) {
        if (usuarioRepository.existsByNome(usuario.getNome())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ja existe um usuario com o nome \"" + usuario.getNome() + "\".");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter pelo menos 6 caracteres.");
        }
        if (usuario.getRole() == null || (!usuario.getRole().equals("ADMIN") && !usuario.getRole().equals("OPERADOR"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Role invalida. Use ADMIN ou OPERADOR.");
        }
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(Integer id, Usuario detalhes) {
        return usuarioRepository.findById(id).map(u -> {
            if (detalhes.getNome() != null) u.setNome(detalhes.getNome());
            if (detalhes.getEmail() != null) u.setEmail(detalhes.getEmail());
            if (detalhes.getRole() != null) u.setRole(detalhes.getRole());
            return usuarioRepository.save(u);
        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public boolean alterarSenha(String username, String senhaAtual, String novaSenha, PasswordEncoder encoder) {
        return usuarioRepository.findByNome(username)
                .map(u -> {
                    if (!encoder.matches(senhaAtual, u.getSenha())) return false;
                    u.setSenha(encoder.encode(novaSenha));
                    usuarioRepository.save(u);
                    return true;
                })
                .orElse(false);
    }
}