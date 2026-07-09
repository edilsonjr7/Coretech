package coretech.sistemaCoreTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import coretech.sistemaCoreTech.model.Usuario;
import java.util.List;
import java.util.Optional;



@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // aqui encontra todos os meus usuarios cadastrados
    Optional<Usuario> findByNome(String nome);

    // verifica se o nome ja existe
        boolean existsByNome(String nome);

        // verifica se  o email ja existe
        boolean existsByEmail(String email);

          Optional<Usuario> findByEmail(String email);

        


    

    
}
