package coretech.sistemaCoreTech.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;
    private final boolean emailHabilitado;
    private final String smtpHost;
    private final String smtpUsuario;

    @Value("${app.pdf.directory:./comprovantes}")
    private String pdfDirectory;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.email.habilitado:true}") boolean emailHabilitado,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${spring.mail.username:}") String smtpUsuario) {
        this.mailSender = mailSender;
        this.emailHabilitado = emailHabilitado;
        this.smtpHost = smtpHost;
        this.smtpUsuario = smtpUsuario;
    }

    // informa se o envio real do comprovante de compra está ligado (app.email.habilitado)
    public boolean isEmailHabilitado() {
        return emailHabilitado;
    }

    /**
     * Envia o código de 6 dígitos que confirma o cadastro do cliente.
     *
     * O envio é SEMPRE real (SMTP). Não existe mais modo fictício: o código não vai
     * para o log nem para a resposta da API. Se o SMTP não estiver configurado ou o
     * envio falhar, lança EmailNaoEnviadoException — a conta continua inativa e o
     * cliente pode solicitar o reenvio do código.
     */
    public String enviarCodigoConfirmacao(String para, String nome, String codigo, int minutosValidade) {
        validarSmtpConfigurado(para);

        String primeiroNome = (nome != null && !nome.isBlank()) ? nome.trim().split("\\s+")[0] : "cliente";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(para);
            helper.setSubject("CoreTech - Confirme seu cadastro");
            helper.setText(textoCodigo(primeiroNome, codigo, minutosValidade),
                    htmlCodigo(primeiroNome, codigo, minutosValidade));
            mailSender.send(message);

            LOG.info("Código de confirmação enviado para: " + para);
            return "Código de confirmação enviado para " + para;
        } catch (EmailNaoEnviadoException e) {
            throw e;
        } catch (Exception e) {
            LOG.warning("Falha ao enviar o código de confirmação para " + para + ": " + e.getMessage());
            throw new EmailNaoEnviadoException("Não foi possível enviar o e-mail de confirmação para " + para
                    + ". Motivo: " + mensagemDoErro(e) + ". Confira as configurações de SMTP no arquivo .env.", e);
        }
    }

    // sem SMTP configurado não existe envio: falha na hora, sem "código de mentira"
    private void validarSmtpConfigurado(String para) {
        if (smtpHost == null || smtpHost.isBlank() || smtpUsuario == null || smtpUsuario.isBlank()) {
            LOG.warning("Tentativa de enviar e-mail para " + para + " sem SMTP configurado.");
            throw new EmailNaoEnviadoException(
                    "O envio de e-mail não está configurado: preencha SMTP_HOST, SMTP_PORT, SMTP_USER e "
                            + "SMTP_PASSWORD no arquivo .env (no Gmail, use uma senha de app).");
        }
    }

    private String mensagemDoErro(Exception e) {
        String mensagem = e.getMessage();
        return (mensagem == null || mensagem.isBlank()) ? e.getClass().getSimpleName() : mensagem;
    }

    private String textoCodigo(String nome, String codigo, int minutos) {
        return "Olá, " + nome + "!\n\n"
                + "Seu código de confirmação na CoreTech é: " + codigo + "\n\n"
                + "Digite esse código na tela de confirmação para ativar a sua conta e liberar o login.\n"
                + "O código é válido por " + minutos + " minutos.\n\n"
                + "Se você não fez esse cadastro, ignore este e-mail: nenhuma conta será ativada.";
    }

    private String htmlCodigo(String nome, String codigo, int minutos) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;color:#1f2937;max-width:520px\">"
                + "<h2 style=\"color:#12b5d0;margin:0 0 12px\">CoreTech</h2>"
                + "<p>Olá, <strong>" + nome + "</strong>!</p>"
                + "<p>Este é o seu código de confirmação de cadastro:</p>"
                + "<p style=\"font-size:32px;font-weight:700;letter-spacing:10px;background:#f2f6f8;"
                + "padding:16px;text-align:center;border-radius:8px\">" + codigo + "</p>"
                + "<p>Digite o código na tela de confirmação para <strong>ativar a sua conta e liberar o login</strong>. "
                + "Ele é válido por " + minutos + " minutos.</p>"
                + "<p style=\"font-size:12px;color:#6b7280\">Se você não fez esse cadastro, ignore este e-mail: "
                + "nenhuma conta será ativada.</p>"
                + "</div>";
    }

    public String enviarComprovante(String para, String assunto, byte[] anexoPdf, String nomeAnexo) {
        if (!emailHabilitado) {
            // Modo sem SMTP configurado: salva o PDF no disco
            String arquivoSalvo = salvarPdfEmDisco(anexoPdf, nomeAnexo);
            LOG.info("E-mail desabilitado. Comprovante salvo em: " + arquivoSalvo);
            return "E-mail desabilitado. Comprovante salvo em disco: " + arquivoSalvo;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText("Olá! Segue em anexo o comprovante da sua compra na CoreTech. Obrigado pela preferência!");
            helper.addAttachment(nomeAnexo, new org.springframework.core.io.ByteArrayResource(anexoPdf));
            mailSender.send(message);
            LOG.info("Comprovante enviado para: " + para);
            return "Comprovante enviado para " + para;
        } catch (Exception e) {
            LOG.warning("Falha ao enviar e-mail: " + e.getMessage());
            String arquivoSalvo = salvarPdfEmDisco(anexoPdf, nomeAnexo);
            return "Falha ao enviar e-mail. Comprovante salvo em disco: " + arquivoSalvo;
        }
    }

    private String salvarPdfEmDisco(byte[] pdf, String nome) {
        try {
            Path dir = Paths.get(pdfDirectory);
            Files.createDirectories(dir);
            Path arquivo = dir.resolve(nome);
            Files.write(arquivo, pdf);
            return arquivo.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar PDF em disco", e);
        }
    }
}
