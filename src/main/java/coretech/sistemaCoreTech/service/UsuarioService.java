package coretech.sistemaCoreTech.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Service // sobe junto com o servidor para utilizar os métodos
public class UsuarioService {

  
   @Autowired
   private  UsuarioRepository usuarioRepository;

    // listar os usuarios
     public List<Usuario> findAll(){
        return usuarioRepository.findAll();
     }

     // busco por ID
     public Usuario buscarIdUsuario(Long id){
        return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException(" Usurio não encontrado!!!1"));
        //orElseThrow() -> new RuntimeException caso o usuario não esteja no banco ele retorna dizendo não encontrado
     }



       public Usuario findById(Long id){
        Optional<Usuario> obj =  usuarioRepository.findById(id);
        return obj.get();
    
}



     // salva o usuario
     public Usuario salvar(Usuario usuario){
        return usuarioRepository.save(usuario);
     }

     // atualiza o usuario
     public Usuario atualizar(Long id, Usuario usuarioAtualizaUsuario){

        Usuario usuario = buscarIdUsuario(id);

        usuario.setNome(usuarioAtualizaUsuario.getNome());
        usuario.setEmail(usuarioAtualizaUsuario.getEmail());

        return usuarioRepository.save(usuario);

     }

   


    




    
    
}
