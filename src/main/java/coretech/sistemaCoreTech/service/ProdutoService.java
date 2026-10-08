package coretech.sistemaCoreTech.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import coretech.sistemaCoreTech.model.Produto;
import coretech.sistemaCoreTech.repository.ProdutoRepository;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    public Produto findById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Produto salvar(Produto produto) {
        if (produto.getPreco() != null && produto.getPreco().signum() < 0) {
            throw new RuntimeException("Preço não pode ser negativo");
        }
        if (produto.getEstoque() != null && produto.getEstoque() < 0) {
            throw new RuntimeException("Estoque não pode ser negativo");
        }
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produto = findById(id);
        produto.setNome(produtoAtualizado.getNome());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setEstoque(produtoAtualizado.getEstoque());
        produto.setCategoria(produtoAtualizado.getCategoria());
        produto.setImagemProduto(produtoAtualizado.getImagemProduto());
        produto.setSpecs(produtoAtualizado.getSpecs());
        return produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        Produto produto = findById(id);
        produtoRepository.delete(produto);
    }
}
