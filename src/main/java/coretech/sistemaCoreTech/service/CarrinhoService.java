package coretech.sistemaCoreTech.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import coretech.sistemaCoreTech.model.Carrinho;
import coretech.sistemaCoreTech.model.ItemCarrinho;
import coretech.sistemaCoreTech.model.Produto;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.CarrinhoRepository;
import coretech.sistemaCoreTech.repository.ProdutoRepository;
import coretech.sistemaCoreTech.repository.UsuarioRepository;

@Service
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Carrinho buscarCarrinhoDoUsuario(Usuario usuario) {
        return carrinhoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrinho novo = new Carrinho();
                    novo.setUsuario(usuario);
                    return carrinhoRepository.save(novo);
                });
    }

    @Transactional
    public Carrinho adicionarItem(Usuario usuario, Long produtoId, Integer quantidade) {
        Carrinho carrinho = buscarCarrinhoDoUsuario(usuario);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getEstoque() == null || produto.getEstoque() < quantidade) {
            throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        // verifica se o produto já está no carrinho
ItemCarrinho itemExistente = null;
        for (ItemCarrinho item : carrinho.getItens()) {
            if (item.getProduto().getId() == produtoId) {
                itemExistente = item;
                break;
            }
        }

        if (itemExistente != null) {
            itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade);
            itemExistente.setPreco(produto.getPreco());
        } else {
            ItemCarrinho novoItem = new ItemCarrinho();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(quantidade);
            novoItem.setPreco(produto.getPreco());
            carrinho.getItens().add(novoItem);
        }

        carrinho.recalcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho removerItem(Usuario usuario, Long itemId) {
        Carrinho carrinho = buscarCarrinhoDoUsuario(usuario);
        carrinho.getItens().removeIf(item -> item.getId().equals(itemId));
        carrinho.recalcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public void limparCarrinho(Usuario usuario) {
        Carrinho carrinho = buscarCarrinhoDoUsuario(usuario);
        carrinho.getItens().clear();
        carrinho.recalcularTotal();
        carrinhoRepository.save(carrinho);
    }
}
