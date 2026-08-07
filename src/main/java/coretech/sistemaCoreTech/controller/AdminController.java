package coretech.sistemaCoreTech.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.service.LoggedUsersService;
import coretech.sistemaCoreTech.service.UsuarioService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LoggedUsersService loggedUsersService;

    @Autowired
    private UsuarioService usuarioService;

    // Painel: todos os usuários logados
    @GetMapping("/usuarios-logados")
    public ResponseEntity<List<Map<String, String>>> usuariosLogados() {
        return ResponseEntity.ok(loggedUsersService.listarLogados());
    }

    // Painel: todos os usuários cadastrados
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> todosUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }
}
