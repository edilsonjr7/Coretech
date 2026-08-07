package coretech.sistemaCoreTech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coretech.sistemaCoreTech.dto.PedidoResponse;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.security.UserDetailsImpl;
import coretech.sistemaCoreTech.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    private Usuario usuarioLogado(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUsuario();
    }

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponse> checkout(Authentication authentication) {
        PedidoResponse resposta = pedidoService.realizarCompra(usuarioLogado(authentication));
        return ResponseEntity.ok(resposta);
    }
}
