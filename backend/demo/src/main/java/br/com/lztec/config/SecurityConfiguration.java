package br.com.lztec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // habilita @PreAuthorize nos controllers
public class SecurityConfiguration {

    private final DatabaseUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(DatabaseUserDetailsService userDetailsService,
                                 JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(List.of("Authorization", "X-User-Role"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                // REQUISITO BANCA: login com token (JWT) -> sem sessao no servidor
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // REQUISITO BANCA: RBAC - controle de nivel de acesso por role (ADMIN/OPERADOR)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/css/**", "/js/**", "/imagens/**", "/h2-console/**").permitAll()

                        // ── Login: gera o token, deve ser publico ────────────────
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // ── Alterar senha: qualquer autenticado ──────────────────
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/alterar-senha").authenticated()

                        // ── Usuários: só ADMIN pode gerenciar ────────────────────
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // ── Endpoint de perfil: qualquer autenticado ─────────────
                        .requestMatchers(HttpMethod.GET, "/api/auth/perfil").authenticated()

                        // ── DELETE: só ADMIN ─────────────────────────────────────
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // ── Cadastros (POST/PUT): só ADMIN ────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/produtos/**").hasRole("ADMIN")

                        // ── Tudo mais: qualquer autenticado ──────────────────────
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // REQUISITO BANCA: validacao do token a cada requisicao
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}