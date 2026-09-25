package coretech.sistemaCoreTech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coretech.sistemaCoreTech.dto.AuthRequest;
import coretech.sistemaCoreTech.dto.AuthResponse;
import coretech.sistemaCoreTech.dto.CadastroRequest;
import coretech.sistemaCoreTech.dto.CodigoResponse;
import coretech.sistemaCoreTech.dto.ConfirmarCodigoRequest;
import coretech.sistemaCoreTech.dto.ReenviarCodigoRequest;
import coretech.sistemaCoreTech.enums.Role;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;
import coretech.sistemaCoreTech.security.JwtService;
import coretech.sistemaCoreTech.service.LoggedUsersService;
import coretech.sistemaCoreTech.service.VerificacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LoggedUsersService loggedUsersService;

    @Autowired
    private VerificacaoService verificacaoService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        }

        if (!usuario.isAtivo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuário não ativado. Digite o código de 6 números enviado para o seu e-mail para confirmar o cadastro.");
        }

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getId());
        loggedUsersService.registrarLogin(token, usuario.getEmail());

        AuthResponse resposta = new AuthResponse(token, usuario.getEmail(), usuario.getNome(), usuario.getRole());
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            loggedUsersService.registrarLogout(authorization.substring(7));
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@Valid @RequestBody CadastroRequest request) {
        Usuario existente = usuarioRepository.findByEmail(request.getEmail()).orElse(null);

        // conta já confirmada: não permite cadastrar o mesmo e-mail de novo
        if (existente != null && existente.isAtivo()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }

        if (existente == null && usuarioRepository.existsByNome(request.getNome())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário já em uso");
        }

        // usuário novo ou cadastro anterior que ainda não foi confirmado
        Usuario usuario = existente != null ? existente : new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setRole(Role.USER); // cadastro público cria SEMPRE usuário comum (cliente)
        usuario.setAtivo(false);

        // gera o código de 6 números, salva o usuário inativo e envia o código por e-mail
        VerificacaoService.Envio envio = verificacaoService.gerarEEnviar(usuario);

        String mensagem = envio.isEnviado()
                ? "Cadastro realizado! Enviamos um código de 6 números para " + usuario.getEmail()
                        + ". Digite o código para confirmar a criação da conta."
                : "Cadastro realizado! O envio de e-mail está desabilitado neste ambiente, use o código abaixo para confirmar a conta.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CodigoResponse(mensagem, usuario.getEmail(), envio.isEnviado(), envio.getCodigoDev()));
    }

    @PostMapping("/confirmar-codigo")
    public ResponseEntity<?> confirmarCodigo(@Valid @RequestBody ConfirmarCodigoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }

        if (usuario.isAtivo()) {
            return ResponseEntity.ok("Conta já confirmada. Faça login para continuar.");
        }

        if (usuario.getCodigoConfirmacao() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nenhum código pendente. Solicite o reenvio do código.");
        }

        if (verificacaoService.codigoExpirado(usuario)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Código expirado. Solicite o reenvio do código.");
        }

        if (!verificacaoService.codigoConfere(usuario, request.getCodigo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código incorreto");
        }

        verificacaoService.confirmar(usuario);

        return ResponseEntity.ok("Conta confirmada com sucesso! Faça login para continuar.");
    }

    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }

        if (usuario.isAtivo()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conta já confirmada. Faça login para continuar.");
        }

        VerificacaoService.Envio envio = verificacaoService.gerarEEnviar(usuario);

        String mensagem = envio.isEnviado()
                ? "Enviamos um novo código de 6 números para " + usuario.getEmail() + "."
                : "Envio de e-mail desabilitado neste ambiente, use o novo código abaixo.";

        return ResponseEntity.ok(new CodigoResponse(mensagem, usuario.getEmail(), envio.isEnviado(), envio.getCodigoDev()));
    }
}
