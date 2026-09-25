package coretech.sistemaCoreTech.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

/**
 * Regra de negócio da confirmação de cadastro:
 * gera o código de 6 números, envia por e-mail, valida e ativa o usuário.
 *
 * O usuário só consegue logar depois que o código é confirmado
 * (o campo `ativo` da entidade Usuario é usado pelo Spring Security em isEnabled()).
 */
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
     * Gera um novo código, deixa o usuário inativo, salva no banco e envia por e-mail.
     * Retorna o resultado do envio (e o código somente quando o e-mail está desabilitado).
     */
    public Envio gerarEEnviar(Usuario usuario) {
        String codigo = String.format("%06d", SORTEIO.nextInt(1_000_000));

        usuario.setCodigoConfirmacao(codigo);
        usuario.setCodigoExpiraEm(LocalDateTime.now().plusMinutes(minutosExpiracao));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        String detalhe = emailService.enviarCodigoConfirmacao(usuario.getEmail(), usuario.getNome(), codigo);
        boolean enviado = emailService.isEmailHabilitado();

        return new Envio(enviado, detalhe, enviado ? null : codigo);
    }

    public boolean codigoExpirado(Usuario usuario) {
        LocalDateTime expiraEm = usuario.getCodigoExpiraEm();
        return expiraEm == null || expiraEm.isBefore(LocalDateTime.now());
    }

    public boolean codigoConfere(Usuario usuario, String codigo) {
        String armazenado = usuario.getCodigoConfirmacao();
        return armazenado != null && codigo != null && armazenado.equals(codigo.trim());
    }

    // ativa a conta e descarta o código (não é mais necessário)
    public void confirmar(Usuario usuario) {
        usuario.setAtivo(true);
        usuario.setCodigoConfirmacao(null);
        usuario.setCodigoExpiraEm(null);
        usuarioRepository.save(usuario);
    }

    public int getMinutosExpiracao() {
        return minutosExpiracao;
    }

    // resultado do envio do código
    public static class Envio {

        private final boolean enviado;
        private final String detalhe;
        private final String codigoDev;

        public Envio(boolean enviado, String detalhe, String codigoDev) {
            this.enviado = enviado;
            this.detalhe = detalhe;
            this.codigoDev = codigoDev;
        }

        public boolean isEnviado() {
            return enviado;
        }

        public String getDetalhe() {
            return detalhe;
        }

        public String getCodigoDev() {
            return codigoDev;
        }
    }
}
