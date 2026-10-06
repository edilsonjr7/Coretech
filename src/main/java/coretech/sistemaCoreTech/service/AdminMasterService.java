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
