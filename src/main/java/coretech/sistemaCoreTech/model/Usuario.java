package coretech.sistemaCoreTech.model;

import javax.management.relation.Role;

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
    private long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nome;

    @Column(unique = true, nullable = false, length = 45)
    private String email;

   @Column(nullable = false, length =60) // lenght é como o varchar e nullabre é not null no SQL
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role; // usuario ou adm

    private boolean ativo=false; // o perfil é criado após a confirmação no email

    private String tokenConfimrado;



    public long getId() {
        return id;
    }


    public void setId(long id) {
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


    public String getTokenConfimrado() {
        return tokenConfimrado;
    }

    
    public void setTokenConfimrado(String tokenConfimrado) {
        this.tokenConfimrado = tokenConfimrado;
    }






    


}
