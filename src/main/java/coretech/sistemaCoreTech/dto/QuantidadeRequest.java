package coretech.sistemaCoreTech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo de PUT /carrinho/itens/{itemId}: nova quantidade de um item já existente no carrinho.
 */
public class QuantidadeRequest {

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade;

    public QuantidadeRequest() {
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
