package br.com.lztec.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String email;

    @Column(nullable = false)
    private String senha;

    // ROLE: ADMIN ou OPERADOR
    @Column(nullable = false)
    private String role = "OPERADOR";

    public Usuario() {}

    public Usuario(String nome, String email, String senha, String role) {
        this.nome  = nome;
        this.email = email;
        this.senha = senha;
        this.role  = role;
    }

    public int getId()    { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome()  { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getRole()  { return role; }
    public void setRole(String role) { this.role = role; }
}
