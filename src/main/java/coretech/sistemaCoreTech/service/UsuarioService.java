package coretech.sistemaCoreTech.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;
import coretech.sistemaCoreTech.enums.Role;

@Service // sobe junto com o servidor para utilizar os métodos
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService EmailService;

    

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

 

    // listar os usuarios
     public List<Usuario> listarUsuarios(){
        return usuarioRepository.findAll();
     }

     // busco por ID
     public Usuario buscarIdUsuario(Long id){
        return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException(" Usurio não encontrado!!!1"));
        //orElseThrow(() -> new RuntimeException caso o usuario não esteja no banco ele retorna dizendo não encontrado
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

     // deletar usuario
     public Usuario excluir(Long id){
        Usuario usuario = buscarIdUsuario(id);
        usuarioRepository.delete(usuario);
     }



    
    
}
