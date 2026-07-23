package br.com.lztec.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * REQUISITO BANCA: validacao do token (JWT) a cada requisicao.
 *
 * Le o header "Authorization: Bearer <token>", valida o token e,
 * se valido, autentica o usuario no contexto de seguranca do Spring.
 * Substitui o antigo fluxo de Basic Auth.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DatabaseUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, DatabaseUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String nomeUsuario = null;
        try {
            nomeUsuario = jwtService.extrairUsuario(token);
        } catch (Exception e) {
            // Token invalido, expirado ou malformado: segue sem autenticar.
            // As regras de authorizeHttpRequests vao bloquear o acesso depois.
            filterChain.doFilter(request, response);
            return;
        }

        if (nomeUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails usuario = userDetailsService.loadUserByUsername(nomeUsuario);

            if (jwtService.tokenValido(token, usuario.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}