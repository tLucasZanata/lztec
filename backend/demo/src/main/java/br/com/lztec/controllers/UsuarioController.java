package br.com.lztec.controllers;

import br.com.lztec.dto.AlterarSenhaRequest;
import br.com.lztec.model.Usuario;
import br.com.lztec.Service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService  = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listarTodos() {
        // Nunca retorna a senha
        List<Usuario> lista = usuarioService.findAll();
        lista.forEach(u -> u.setSenha(null));
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        return usuarioService.findById(id)
                .map(u -> { u.setSenha(null); return ResponseEntity.ok(u); })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
        Usuario novo = usuarioService.save(usuario, passwordEncoder);
        novo.setSenha(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Integer id,
                                                    @RequestBody Usuario detalhes) {
        Usuario atualizado = usuarioService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        atualizado.setSenha(null);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<String> alterarSenha(@RequestBody AlterarSenhaRequest req) {
        if (req.getUsername() == null || req.getSenhaAtual() == null || req.getNovaSenha() == null)
            return ResponseEntity.badRequest().body("Campos obrigatórios ausentes.");
        if (req.getNovaSenha().length() < 6)
            return ResponseEntity.badRequest().body("A nova senha deve ter pelo menos 6 caracteres.");
        return usuarioService.alterarSenha(req.getUsername(), req.getSenhaAtual(), req.getNovaSenha(), passwordEncoder)
                ? ResponseEntity.ok("Senha alterada com sucesso.")
                : ResponseEntity.status(401).body("Usuário ou senha atual incorretos.");
    }
}
