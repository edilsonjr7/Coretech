package coretech.sistemaCoreTech.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;


@Service
public class VerificacaoService {

    private static final SecureRandom SORTEIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final int minutosExpiracao;

    public VerificacaoService(UsuarioRepository usuarioRepository,
            EmailService emailService,
            @Value("${app.verificacao.codigo-expiracao-minutos:15}") int minutosExpiracao) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.minutosExpiracao = minutosExpiracao;
    }

    /**
     * Gera um novo código de 6 dígitos, deixa o usuário inativo, salva no banco e
     * envia por e-mail (envio real).
     *
     * Se o envio falhar, propaga EmailNaoEnviadoException: o código fica salvo e o
     * cliente pode pedir o reenvio. O código NUNCA é devolvido para a API.
     *
     * @return detalhe do envio (para a mensagem mostrada ao cliente)
     */
    public String gerarEEnviar(Usuario usuario) {
        String codigo = String.format("%06d", SORTEIO.nextInt(1_000_000));

        usuario.setCodigoConfirmacao(codigo);
        usuario.setCodigoExpiraEm(LocalDateTime.now().plusMinutes(minutosExpiracao));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        return emailService.enviarCodigoConfirmacao(usuario.getEmail(), usuario.getNome(), codigo, minutosExpiracao);
    }

    public boolean codigoExpirado(Usuario usuario) {
        LocalDateTime expiraEm = usuario.getCodigoExpiraEm();
        return expiraEm == null || expiraEm.isBefore(LocalDateTime.now());
    }

    public boolean codigoConfere(Usuario usuario, String codigo) {
        String armazenado = usuario.getCodigoConfirmacao();
        return armazenado != null && codigo != null && armazenado.equals(codigo.trim());
    }

   
    public void confirmar(Usuario usuario) {
        usuario.setAtivo(true);
        usuario.setCodigoConfirmacao(null);
        usuario.setCodigoExpiraEm(null);
        usuarioRepository.save(usuario);
    }

    public int getMinutosExpiracao() {
        return minutosExpiracao;
    }
}
