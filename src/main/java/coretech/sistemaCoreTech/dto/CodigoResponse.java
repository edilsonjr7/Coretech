package coretech.sistemaCoreTech.dto;

/**
 * Resposta do cadastro e do reenvio do código de confirmação.
 *
 * O código de 6 dígitos NÃO faz parte da resposta: ele só existe no e-mail enviado
 * ao cliente (envio real por SMTP). Não há mais modo "fictício"/dev.
 */
public class CodigoResponse {

    private String mensagem;
    private String email;
    private boolean emailEnviado;

    public CodigoResponse() {
    }

    public CodigoResponse(String mensagem, String email, boolean emailEnviado) {
        this.mensagem = mensagem;
        this.email = email;
        this.emailEnviado = emailEnviado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(boolean emailEnviado) {
        this.emailEnviado = emailEnviado;
    }
}
