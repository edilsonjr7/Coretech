package coretech.sistemaCoreTech.dto;

import coretech.sistemaCoreTech.enums.Role;

public class AuthResponse {

    private String token;
    private String email;
    private String nome;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String nome, Role role) {
        this.token = token;
        this.email = email;
        this.nome = nome;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
