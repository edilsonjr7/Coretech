package coretech.sistemaCoreTech.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private String categoria;

    @ElementCollection
    private List<String> imagemProduto = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<String> getImagemProduto() {
        return imagemProduto;
    }

    public void setImagemProduto(List<String> imagemProduto) {
        this.imagemProduto = imagemProduto;
    }

    public Produto(long id, String nome, String descricao, BigDecimal preco, Integer estoque,
            String categoria, List<String> imagemProduto) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.imagemProduto = imagemProduto != null ? imagemProduto : new ArrayList<>();
    }

    public Produto(long id, String nome, String descricao, BigDecimal preco, Integer estoque,
            List<String> imagemProduto) {
        this(id, nome, descricao, preco, estoque, "Consoles", imagemProduto);
    }

    public Produto() {
    }
}
