package coretech.sistemaCoreTech.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
// Seed idempotente: roda nos perfis "test" (H2 em memoria) e "prod" (MySQL) — so cria
// o que ainda nao existe, entao nunca duplica dados. Garante que a loja tenha produtos
// ao migrar para o banco MySQL.
@Profile({ "test", "prod" })
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

    // ==== ESPECIFICACOES PADRAO DE CADA PRODUTO ====
    // Mude os valores abaixo para alterar as specs iniciais de cada produto.
    // Servem tambem para preencher bancos criados antes do campo "specs" existir.
    private static final Map<String, List<String>> SPECS_PADRAO = Map.ofEntries(
            Map.entry("PlayStation 5", List.of("SSD 825GB", "4K @ 120Hz", "GPU 10.28 TFLOPS", "Ray Tracing")),
            Map.entry("Xbox Series X", List.of("SSD 1TB", "4K @ 120Hz", "GPU 12 TFLOPS", "Quick Resume")),
            Map.entry("Nintendo Switch OLED", List.of("Tela OLED 7\"", "64GB", "Modo Híbrido", "Joy-Con")),
            Map.entry("Controle DualSense", List.of("Bluetooth 5.1", "Feedback Háptico", "Gatilhos Adaptativos", "Bateria 12h")),
            Map.entry("Controle Xbox Elite", List.of("Paddles", "Sticks Intercambiáveis", "Bluetooth", "App Xbox")),
            Map.entry("Headset Gamer Pro", List.of("Surround 7.1", "Microfone com Noise Gate", "RGB", "USB/3.5mm")),
            Map.entry("Headset Sem Fio Pulse", List.of("Áudio 3D", "Bateria 30h", "Sem Fio", "Microfone Retrátil")),
            Map.entry("Teclado Mecânico RGB", List.of("Switches Blue", "RGB por Tecla", "Alumínio", "Anti-Ghosting")));

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

        // ==== Backfill das especificacoes ====
        // O banco MySQL ja tinha produtos criados antes de o campo "specs" existir,
        // entao eles voltariam sem especificacoes. Preenche as specs padrao de cada
        // produto que ainda nao tem nenhuma. O backfill so roda enquanto NENHUM
        // produto tem specs, assim edicoes feitas pelo painel admin nunca sao
        // sobrescritas nas proximas inicializacoes.
        List<Produto> todosProdutos = produtoRepository.findAll();
        boolean algumProdutoComSpecs = todosProdutos.stream()
                .anyMatch(p -> p.getSpecs() != null && !p.getSpecs().isEmpty());
        if (!algumProdutoComSpecs) {
            for (Produto produto : todosProdutos) {
                List<String> specs = SPECS_PADRAO.get(produto.getNome());
                if (specs != null) {
                    produto.setSpecs(specs);
                    produtoRepository.save(produto);
                }
            }
        }

        System.out.println(">>> Seed de cliente e produtos gamers inserido no banco com sucesso <<<");
    }
}