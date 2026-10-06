package coretech.sistemaCoreTech.service;

/**
 * Lançada quando o envio real de e-mail (SMTP) não foi possível.
 *
 * O projeto NÃO usa mais código fictício: se o SMTP não estiver configurado
 * ou o provedor recusar o envio, a operação falha com esta exceção em vez de
 * "inventar" um código no log/resposta da API.
 */
public class EmailNaoEnviadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailNaoEnviadoException(String mensagem) {
        super(mensagem);
    }

    public EmailNaoEnviadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
