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
import coretech.sistemaCoreTech.enums.Role;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;
import coretech.sistemaCoreTech.security.JwtService;
import coretech.sistemaCoreTech.service.LoggedUsersService;

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
                    .body("Usuário não ativado. Verifique seu e-mail.");
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
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }
        if (usuarioRepository.existsByNome(request.getNome())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário já em uso");
        }

        Usuario usuario = new Usuario(null, request.getNome(), request.getEmail(),
                passwordEncoder.encode(request.getSenha()), Role.USER, true);
        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso");
    }
}
