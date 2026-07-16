package coretech.sistemaCoreTech.config;

import java.util.Arrays;

import coretech.sistemaCoreTech.enums.Role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.ProdutoRepository;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Configuration
@Profile("test")
public class TesteConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder; // injeta o mesmo encoder do SecurityConfig

    public TesteConfig(UsuarioRepository usuarioRepository, ProdutoRepository produtoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.passwordEncoder = passwordEncoder; // a senha vira uma hash de criptografia @a$5sfd para proteger a senha e não usar senha String
    }

    @Override
    public void run(String... args) throws Exception {

        Usuario usuario = new Usuario(null, "tom", "tom@gmail.com", passwordEncoder.encode("123456"), Role.USER, true);
        Usuario usuario2 = new Usuario(null, "ana", "ana@gmail.com", passwordEncoder.encode("6767"), Role.ADMIN, true);

        usuarioRepository.saveAll(Arrays.asList(usuario, usuario2));

        System.out.println(">>> Seed de usuários de teste inserido com sucesso <<<");
    }
}