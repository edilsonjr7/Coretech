package coretech.sistemaCoreTech.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import coretech.sistemaCoreTech.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // ela diz "trate como uma tabela do banco de dados"
@Table(name="usuarios") // nome da tabela
public class Usuario {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)// gera o ID do usuario no banco de dados
    @Column(name="id", unique=true) // aqui dei o nome da coluna e usei o unique para um unico valor do ID no0 banco
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nome;

    @Column(unique = true, nullable = false, length = 45)
    private String email;

   @Column(nullable = false, length =60) // lenght é como o varchar e nullabre é not null no SQL
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role; // usuario ou adm

    private boolean ativo=false; // o perfil é criado após a confirmação no email

    // código de 6 números enviado por e-mail para confirmar o cadastro do cliente
    @Column(name = "codigo_confirmacao", length = 6)
    private String codigoConfirmacao;

    // data/hora em que o código de confirmação deixa de ser válido
    @Column(name = "codigo_expira_em")
    private LocalDateTime codigoExpiraEm;

   

   



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getNome() {
        return nome;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    // @JsonIgnore: a senha (hash) nunca é devolvida nas respostas JSON da API
    @JsonIgnore
    public String getSenha() {
        return senha;
    }


    public void setSenha(String senha) {
        this.senha = senha;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(Role role) {
        this.role = role;
    }


    public boolean isAtivo() {
        return ativo;
    }


    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // @JsonIgnore: o código de confirmação não pode aparecer nas respostas JSON
    @JsonIgnore
    public String getCodigoConfirmacao() {
        return codigoConfirmacao;
    }

    public void setCodigoConfirmacao(String codigoConfirmacao) {
        this.codigoConfirmacao = codigoConfirmacao;
    }

    @JsonIgnore
    public LocalDateTime getCodigoExpiraEm() {
        return codigoExpiraEm;
    }

    public void setCodigoExpiraEm(LocalDateTime codigoExpiraEm) {
        this.codigoExpiraEm = codigoExpiraEm;
    }


    public Usuario(Long id, String nome, String email, String senha, Role role, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.ativo = ativo;
    }

    public Usuario(){
        
    }






    


}
