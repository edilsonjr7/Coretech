package coretech.sistemaCoreTech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// corpo da requisição que gera e envia um novo código de confirmação
public class ReenviarCodigoRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    public ReenviarCodigoRequest() {
    }

    public ReenviarCodigoRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
