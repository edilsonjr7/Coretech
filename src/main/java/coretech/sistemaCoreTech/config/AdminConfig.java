package coretech.sistemaCoreTech.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.service.AdminMasterService;

/**
 * Garante que no sistema exista apenas UM login com perfil ADMIN (o admin master).
 *
 * Na inicialização:
 *  1. apaga todos os usuários ADMIN diferentes do admin master;
 *  2. cria (ou atualiza) o admin master com o e-mail/senha definidos no application.properties.
 */
@Configuration
@Order(2)
public class AdminConfig implements CommandLineRunner {

    private static final Logger LOG = Logger.getLogger(AdminConfig.class.getName());

    private final AdminMasterService adminMasterService;
    private final String adminEmail;
    private final String adminSenha;
    private final String adminNome;

    public AdminConfig(AdminMasterService adminMasterService,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.senha}") String adminSenha,
            @Value("${app.admin.nome}") String adminNome) {
        this.adminMasterService = adminMasterService;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.adminNome = adminNome;
    }

    @Override
    public void run(String... args) throws Exception {
        int removidos = adminMasterService.removerAdminsExceto(adminEmail);

        if (removidos > 0) {
            LOG.info(">>> " + removidos + " perfil(is) ADMIN antigo(s) removido(s). Somente o admin master permanece. <<<");
        }

        Usuario admin = adminMasterService.garantirAdminMaster(adminNome, adminEmail, adminSenha);

        LOG.info(">>> Perfil ADMIN (master) pronto! Email: " + admin.getEmail() + " <<<");
    }
}