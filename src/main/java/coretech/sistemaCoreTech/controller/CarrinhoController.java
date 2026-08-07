package coretech.sistemaCoreTech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coretech.sistemaCoreTech.model.Carrinho;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.security.UserDetailsImpl;
import coretech.sistemaCoreTech.service.CarrinhoService;

import jakarta.validation.Valid;
import coretech.sistemaCoreTech.dto.ItemCarrinhoRequest;

@RestController
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    private Usuario usuarioLogado(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUsuario();
    }

    @GetMapping
    public ResponseEntity<Carrinho> verCarrinho(Authentication authentication) {
        return ResponseEntity.ok(carrinhoService.buscarCarrinhoDoUsuario(usuarioLogado(authentication)));
    }

    @PostMapping("/itens")
    public ResponseEntity<Carrinho> adicionarItem(Authentication authentication,
            @Valid @RequestBody ItemCarrinhoRequest request) {
        Carrinho carrinho = carrinhoService.adicionarItem(usuarioLogado(authentication),
                request.getProdutoId(), request.getQuantidade());
        return ResponseEntity.ok(carrinho);
    }

    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<Carrinho> removerItem(Authentication authentication, @PathVariable Long itemId) {
        Carrinho carrinho = carrinhoService.removerItem(usuarioLogado(authentication), itemId);
        return ResponseEntity.ok(carrinho);
    }
}
