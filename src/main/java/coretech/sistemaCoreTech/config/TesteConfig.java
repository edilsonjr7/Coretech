package coretech.sistemaCoreTech.config;

import java.util.ArrayList;
import java.util.Arrays;

import coretech.sistemaCoreTech.enums.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.model.Produto;

import coretech.sistemaCoreTech.repository.ProdutoRepository;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Configuration
@Profile("test")
public class TesteConfig implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;


    @Override
    public void run(String... args) throws Exception{

      Usuario usuario = new Usuario(1, "tom", "tom@gmail.com", "123456", Role.USER, false);
      
      Usuario usuario2 = new Usuario(2, "ana", "ana@gmail.com", "6767", Role.ADMIN, true);



     usuarioRepository.saveAll(Arrays.asList(usuario, usuario2));







    }




    
}
