package br.com.lztec.dto;

public class AlterarSenhaRequest {
    private String username;
    private String senhaAtual;
    private String novaSenha;

    public String getUsername()  { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
