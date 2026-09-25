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

    @Value("${app.pdf.directory:./comprovantes}")
    private String pdfDirectory;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.email.habilitado:false}") boolean emailHabilitado) {
        this.mailSender = mailSender;
        this.emailHabilitado = emailHabilitado;
    }

    // informa se o envio real de e-mail está ligado (app.email.habilitado)
    public boolean isEmailHabilitado() {
        return emailHabilitado;
    }

    /**
     * Envia o código de 6 números usado para confirmar o cadastro do cliente.
     * Quando o e-mail está desabilitado (padrão do projeto), o código é registrado
     * no log da aplicação para permitir o teste do fluxo sem SMTP configurado.
     */
    public String enviarCodigoConfirmacao(String para, String nome, String codigo) {
        String texto = "Olá" + (nome != null && !nome.isBlank() ? " " + nome : "")
                + "! Seu código de confirmação na CoreTech é: " + codigo
                + ". Ele expira em poucos minutos. Se você não fez esse cadastro, ignore este e-mail.";

        if (!emailHabilitado) {
            LOG.info("E-mail desabilitado. Código de confirmação de " + para + ": " + codigo);
            return "E-mail desabilitado: o código foi registrado no log da aplicação";
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(para);
            helper.setSubject("CoreTech - Confirme seu cadastro");
            helper.setText(texto);
            mailSender.send(message);
            LOG.info("Código de confirmação enviado para: " + para);
            return "Código de confirmação enviado para " + para;
        } catch (Exception e) {
            LOG.warning("Falha ao enviar o código de confirmação: " + e.getMessage());
            return "Falha ao enviar o e-mail: " + e.getMessage();
        }
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
