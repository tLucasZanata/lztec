package br.com.lztec.controllers;

import br.com.lztec.Service.AuditService;
import br.com.lztec.model.Usuario;
import br.com.lztec.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import br.com.lztec.config.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final AuditService      auditService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository,
                          AuditService auditService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.auditService      = auditService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * REQUISITO BANCA: login que gera o token (JWT).
     * Recebe nome de usuário e senha, valida e devolve o token a ser
     * enviado no header "Authorization: Bearer <token>" nas próximas requisições.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> dados) {
        String nome  = dados.get("nome");
        String senha = dados.get("senha");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(nome, senha));
        } catch (Exception e) {
            auditService.registrarLogin(nome, false);
            return ResponseEntity.status(401).body(Map.of("erro", "Usuário ou senha inválidos."));
        }

        Usuario usuario = usuarioRepository.findByNome(nome).orElseThrow();
        String token = jwtService.gerarToken(usuario.getNome(), usuario.getRole());
        auditService.registrarLogin(usuario.getNome(), true);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "nome",  usuario.getNome(),
                "role",  usuario.getRole()
        ));
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, String>> perfil(Authentication auth) {
        return usuarioRepository.findByNome(auth.getName())
                .map(u -> ResponseEntity.ok(Map.of(
                        "nome",  u.getNome(),
                        "role",  u.getRole(),
                        "email", u.getEmail() != null ? u.getEmail() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}