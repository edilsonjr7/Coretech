package coretech.sistemaCoreTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import coretech.sistemaCoreTech.model.ItemCarrinho;

import java.util.List;
import java.util.Optional;


@Repository
public interface ItemCarrinhoRepository  extends JpaRepository<ItemCarrinho, Long>{

    List<ItemCarrinho> findByCarrinhoId(long id); // busca os itens no carrinho pelo ID

    Optional<ItemCarrinho> findByCarrinhoIdAndProdutoid(Long id, Long idProduto);
    // aqui consigo buscar itens especifícos no meu carrinho
    
    


    
}
