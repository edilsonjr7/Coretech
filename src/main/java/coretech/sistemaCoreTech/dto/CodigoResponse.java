package coretech.sistemaCoreTech.dto;

/**
 * Resposta do cadastro e do reenvio do código de confirmação.
 *
 * IMPORTANTE: o campo `codigoDev` só é preenchido quando o envio de e-mail está
 * desabilitado (app.email.habilitado=false), para permitir testes locais sem SMTP.
 * Com o SMTP configurado, ele volta sempre nulo e o código só chega no e-mail.
 */
public class CodigoResponse {

    private String mensagem;
    private String email;
    private boolean emailEnviado;
    private String codigoDev;

    public CodigoResponse() {
    }

    public CodigoResponse(String mensagem, String email, boolean emailEnviado, String codigoDev) {
        this.mensagem = mensagem;
        this.email = email;
        this.emailEnviado = emailEnviado;
        this.codigoDev = codigoDev;
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

    public String getCodigoDev() {
        return codigoDev;
    }

    public void setCodigoDev(String codigoDev) {
        this.codigoDev = codigoDev;
    }
}
