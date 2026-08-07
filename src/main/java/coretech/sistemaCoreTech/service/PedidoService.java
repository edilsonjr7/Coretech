package coretech.sistemaCoreTech.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import coretech.sistemaCoreTech.dto.PedidoResponse;
import coretech.sistemaCoreTech.model.Carrinho;
import coretech.sistemaCoreTech.model.ItemCarrinho;
import coretech.sistemaCoreTech.model.Pedido;
import coretech.sistemaCoreTech.model.Produto;
import coretech.sistemaCoreTech.model.Usuario;
import coretech.sistemaCoreTech.repository.PedidoRepository;
import coretech.sistemaCoreTech.repository.ProdutoRepository;

@Service
public class PedidoService {

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public PedidoResponse realizarCompra(Usuario usuario) {
        Carrinho carrinho = carrinhoService.buscarCarrinhoDoUsuario(usuario);

        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho vazio. Adicione itens antes de comprar.");
        }

        // Valida estoque e cria o pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getEstoque() == null || produto.getEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para: " + produto.getNome());
            }
            // reduz estoque
            produto.setEstoque(produto.getEstoque() - item.getQuantidade());
            produtoRepository.save(produto);

            ItemCarrinho copia = new ItemCarrinho();
            copia.setProduto(produto);
            copia.setQuantidade(item.getQuantidade());
            copia.setPreco(item.getPreco());
            pedido.getItens().add(copia);

            total = total.add(item.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        pedido.setTotal(total);
        pedidoRepository.save(pedido);

        // gera PDF e envia e-mail
        byte[] pdf = pdfService.gerarComprovante(pedido);
        String nomeArquivo = "comprovante-pedido-" + pedido.getId() + ".pdf";
        String statusEmail = emailService.enviarComprovante(usuario.getEmail(),
                "Comprovante de Compra - Pedido #" + pedido.getId(), pdf, nomeArquivo);

        // limpa carrinho após a compra
        carrinhoService.limparCarrinho(usuario);

        PedidoResponse response = new PedidoResponse();
        response.setId(pedido.getId());
        response.setCliente(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setData(pedido.getData());
        response.setTotal(pedido.getTotal());
        for (ItemCarrinho item : pedido.getItens()) {
            response.getItensDescricao()
                    .add(item.getQuantidade() + "x " + item.getProduto().getNome() + " = R$ "
                            + item.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        response.setStatusEmail(statusEmail);

        return response;
    }
}
