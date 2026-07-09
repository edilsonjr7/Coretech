package coretech.sistemaCoreTech.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.service.UsuarioService;

@RestController
@RequestMapping(name = "/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping(value =  "/user")
   public ResponseEntity<List> findAll(){
    List<Usuario> list = usuarioService.findAll(); 
        return ResponseEntity.ok().body(list);
        // return ResponseEntity.ok() -> retorna a resposta de sucesso HTTP
        // body(list) -> retorna o corpo de dentro da minha lista 
    }

    @GetMapping(value =  "/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Long id){  //@PathVariable faz referencia como o parametro do "/{id}" do GetMapping
        Usuario obj = usuarioService.findById(id);
        return ResponseEntity.ok().body(obj);

    }




    
}
