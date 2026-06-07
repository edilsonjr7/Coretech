package coretech.sistemaCoreTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import coretech.sistemaCoreTech.model.Carrinho;
import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    // vai encotrar o carrinho do usuario logado
    Optional<Carrinho> findByUsuarioId(Long id);


    
}
