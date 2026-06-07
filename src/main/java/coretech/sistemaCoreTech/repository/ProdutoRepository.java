package coretech.sistemaCoreTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import coretech.sistemaCoreTech.model.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome); // vai buscar o produto pelo nome
    // findByNomeContainingIgnoreCase busca o produto ignorando as maiusculas e minusculas

    List<Produto> findByPrecoBetween(BigDecimal min, BigDecimal max); // aqui ele procura o produto pela faixa de preço, sendo alto ou baixo
    




    
}
