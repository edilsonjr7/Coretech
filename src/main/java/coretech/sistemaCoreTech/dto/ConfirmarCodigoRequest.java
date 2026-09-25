package coretech.sistemaCoreTech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// corpo da requisição que confirma o cadastro: e-mail + código de 6 números recebido por e-mail
public class ConfirmarCodigoRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Código é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código deve ter exatamente 6 números")
    private String codigo;

    public ConfirmarCodigoRequest() {
    }

    public ConfirmarCodigoRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
