package coretech.sistemaCoreTech.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import coretech.sistemaCoreTech.model.ItemCarrinho;
import coretech.sistemaCoreTech.model.Pedido;

@Service
public class PdfService {

    public byte[] gerarComprovante(Pedido pedido) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("CoreTech - Comprovante de Compra", titulo));
            document.add(new Paragraph("Pedido #" + pedido.getId(), subtitulo));
            document.add(new Paragraph("Cliente: " + pedido.getUsuario().getNome(), subtitulo));
            document.add(new Paragraph("Email: " + pedido.getUsuario().getEmail(), subtitulo));
            document.add(new Paragraph("Data: " + pedido.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subtitulo));
            document.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(4);
            tabela.setWidthPercentage(100);
            tabela.addCell(celula("Produto", true));
            tabela.addCell(celula("Qtd", true));
            tabela.addCell(celula("Preço Unit.", true));
            tabela.addCell(celula("Subtotal", true));

            for (ItemCarrinho item : pedido.getItens()) {
                tabela.addCell(celula(item.getProduto().getNome(), false));
                tabela.addCell(celula(String.valueOf(item.getQuantidade()), false));
                tabela.addCell(celula("R$ " + item.getPreco(), false));
                tabela.addCell(celula("R$ " + item.getPreco().multiply(java.math.BigDecimal.valueOf(item.getQuantidade())), false));
            }
            document.add(tabela);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total: R$ " + pedido.getTotal(), titulo));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do comprovante", e);
        }
    }

    private PdfPCell celula(String texto, boolean negrito) {
        Font fonte = negrito
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)
                : FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }
}
