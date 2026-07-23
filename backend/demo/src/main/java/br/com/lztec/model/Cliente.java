package br.com.lztec.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// Cadastro de Cliente - Pessoa Fisica (CPF) ou Pessoa Juridica (CNPJ)

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nome;

    // CORREÇÃO: Mapeia "cpf_cnpj" (do JSON) para o atributo Java
    @JsonProperty("cpf_cnpj")
    private String cpfCnpj;

    private String email;
    private String contato;
    private String endereco;

    // Data de nascimento - usada apenas para clientes Pessoa Fisica (CPF)
    @JsonProperty("data_nascimento")
    private LocalDate dataNascimento;

    // CORREÇÃO: Mapeia "endereco_adicional" (do JSON) para o atributo Java
    @JsonProperty("endereco_adicional")
    private String enderecoAdicional;

    // Campos detalhados de endereço. Para Pessoa Juridica (CNPJ) sao
    // preenchidos automaticamente pela consulta de CNPJ; para Pessoa
    // Fisica (CPF), numero e bairro sao preenchidos manualmente e obrigatorios.
    // O campo "endereco" acima e mantido como o logradouro/texto livre.
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String municipio;
    private String uf;
    private String cep;

    public Cliente() {}

    public Cliente(String nome, String cpfCnpj, String email, String contato, String endereco, String enderecoAdicional) {
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.contato = contato;
        this.endereco = endereco;
        this.enderecoAdicional = enderecoAdicional;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEnderecoAdicional() { return enderecoAdicional; }
    public void setEnderecoAdicional(String enderecoAdicional) { this.enderecoAdicional = enderecoAdicional; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}
