package coretech.sistemaCoreTech.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name="itemcarrinho")
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @ManyToOne
    @JoinColumn(name="produto_id") // aqui eu literalmente usei um join entre as colunas, "junte a tabela itemCarrinho com a do produto"
    private Produto produto;

    private Integer quantidade;

    private BigDecimal preco;



    
}
