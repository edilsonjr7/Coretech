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
