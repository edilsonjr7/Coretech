package coretech.sistemaCoreTech.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import coretech.sistemaCoreTech.enums.Role;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Configuration
public class AdminConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminConfig(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Cria o admin padrão se não existir
        if (!usuarioRepository.existsByEmail("admin@coretech.com")) {
            Usuario admin = new Usuario(null, "admin", "admin@coretech.com",
                    passwordEncoder.encode("admin123"), Role.ADMIN, true);
            usuarioRepository.save(admin);
            System.out.println(">>> Perfil ADMIN criado com sucesso! Email: admin@coretech.com / Senha: admin123 <<<");
        }
    }
}