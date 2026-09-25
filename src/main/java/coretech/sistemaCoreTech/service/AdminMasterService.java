package coretech.sistemaCoreTech.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import coretech.sistemaCoreTech.enums.Role;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.CarrinhoRepository;
import coretech.sistemaCoreTech.repository.PedidoRepository;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

/**
 * Manutenção do acesso administrativo do sistema.
 *
 * Regra do sistema: existe UM único login com perfil ADMIN (o admin master).
 * Todo usuário ADMIN diferente dele é removido na inicialização.
 */
@Service
public class AdminMasterService {

    private static final Logger LOG = Logger.getLogger(AdminMasterService.class.getName());

    private final UsuarioRepository usuarioRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminMasterService(UsuarioRepository usuarioRepository,
            CarrinhoRepository carrinhoRepository,
            PedidoRepository pedidoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Apaga todos os usuários com perfil ADMIN, mantendo apenas o e-mail do admin master.
     * Remove antes o carrinho e os pedidos do usuário para não violar chave estrangeira.
     */
    @Transactional
    public int removerAdminsExceto(String emailMaster) {
        List<Usuario> admins = usuarioRepository.findByRole(Role.ADMIN);
        int removidos = 0;

        for (Usuario admin : admins) {
            if (admin.getEmail() != null && admin.getEmail().equalsIgnoreCase(emailMaster)) {
                continue;
            }

            try {
                carrinhoRepository.findByUsuarioId(admin.getId()).ifPresent(carrinhoRepository::delete);
                pedidoRepository.findByUsuarioId(admin.getId()).forEach(pedidoRepository::delete);
                usuarioRepository.delete(admin);
                removidos++;
                LOG.info(">>> Perfil ADMIN removido do banco: " + admin.getEmail());
            } catch (Exception e) {
                LOG.warning(">>> Não foi possível remover o ADMIN " + admin.getEmail() + ": " + e.getMessage());
            }
        }

        return removidos;
    }

    /**
     * Cria o admin master se ainda não existir e garante que as credenciais
     * (nome, senha, perfil ADMIN e conta ativa) estejam sempre válidas.
     */
    @Transactional
    public Usuario garantirAdminMaster(String nome, String email, String senhaPura) {
        Usuario admin = usuarioRepository.findByEmail(email).orElseGet(Usuario::new);

        admin.setNome(nomeDisponivel(nome, email));
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(senhaPura));
        admin.setRole(Role.ADMIN);
        admin.setAtivo(true);
        admin.setCodigoConfirmacao(null);
        admin.setCodigoExpiraEm(null);

        return usuarioRepository.save(admin);
    }

    // o campo "nome" é único no banco: evita erro caso um cliente já use esse nome
    private String nomeDisponivel(String nome, String email) {
        String candidato = nome;
        int sufixo = 1;

        while (true) {
            Optional<Usuario> existente = usuarioRepository.findByNome(candidato);
            if (existente.isEmpty() || email.equalsIgnoreCase(existente.get().getEmail())) {
                return candidato;
            }
            candidato = nome + sufixo++;
        }
    }
}
