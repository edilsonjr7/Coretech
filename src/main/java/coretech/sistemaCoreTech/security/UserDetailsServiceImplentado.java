package coretech.sistemaCoreTech.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImplentado implements UserDetailsService {

    // aqui ele vai buscar o usuario e vai devolver pro UserDetailsimpl
    // Regra de negocio

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImplentado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new UserDetailsImpl(usuario);
    }
}