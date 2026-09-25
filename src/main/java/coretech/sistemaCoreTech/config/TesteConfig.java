package coretech.sistemaCoreTech.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import coretech.sistemaCoreTech.enums.Role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import coretech.sistemaCoreTech.model.Produto;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.ProdutoRepository;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Configuration
@Profile("test")
@Order(1)
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

        // Somente cliente (USER) é criado pelo seed.
        // O único perfil ADMIN do sistema é o admin master, criado pelo AdminConfig.
        if (usuarioRepository.findByEmail("tom@gmail.com").isEmpty()) {
            Usuario usuario = new Usuario(null, "tom", "tom@gmail.com", passwordEncoder.encode("123456"), Role.USER, true);
            usuarioRepository.save(usuario);
        }

        if (produtoRepository.count() == 0) {
            Produto p1 = new Produto(0, "PlayStation 5", "Console de nova geração com SSD ultrarrápido, ray tracing e gráficos 4K.", new BigDecimal("4499.00"), 10, "Consoles", List.of("https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop"));
            Produto p2 = new Produto(0, "Xbox Series X", "O console mais poderoso da Microsoft com 12 TFLOPS de potência e Quick Resume.", new BigDecimal("4299.00"), 8, "Consoles", List.of("https://images.unsplash.com/photo-1621259182978-fbf93132d53d?q=80&w=800&auto=format&fit=crop"));
            Produto p3 = new Produto(0, "Nintendo Switch OLED", "Console híbrido com tela OLED de 7 polegadas e modo portátil e dock.", new BigDecimal("2499.00"), 15, "Consoles", List.of("https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?q=80&w=800&auto=format&fit=crop"));
            Produto p4 = new Produto(0, "Controle DualSense", "Controle sem fio com feedback háptico e gatilhos adaptativos.", new BigDecimal("499.00"), 25, "Controles", List.of("https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=800&auto=format&fit=crop"));
            Produto p5 = new Produto(0, "Controle Xbox Elite", "Controle profissional com paddles traseiros e sticks intercambiáveis.", new BigDecimal("1299.00"), 12, "Controles", List.of("https://images.unsplash.com/photo-1607853202273-797f1c22a38e?q=80&w=800&auto=format&fit=crop"));
            Produto p6 = new Produto(0, "Headset Gamer Pro", "Headset com som surround 7.1, microfone com cancelamento de ruído e RGB.", new BigDecimal("349.00"), 30, "Headsets", List.of("https://images.unsplash.com/photo-1599669454699-248893623440?q=80&w=800&auto=format&fit=crop"));
            Produto p7 = new Produto(0, "Headset Sem Fio Pulse", "Headset sem fio com áudio 3D e bateria de 30 horas de duração.", new BigDecimal("899.00"), 18, "Headsets", List.of("https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?q=80&w=800&auto=format&fit=crop"));
            Produto p8 = new Produto(0, "Teclado Mecânico RGB", "Teclado mecânico com switches blue, RGB por tecla e estrutura em alumínio.", new BigDecimal("599.00"), 22, "Acessórios", List.of("https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=800&auto=format&fit=crop"));
            produtoRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));
        }

        System.out.println(">>> Seed de cliente e produtos gamers inserido no banco com sucesso <<<");
    }
}