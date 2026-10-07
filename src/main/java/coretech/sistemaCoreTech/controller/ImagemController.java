package coretech.sistemaCoreTech.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload de imagens de produtos feito pelo painel admin (arquivo do PC).
 * A rota /admin/** ja exige ROLE_ADMIN (ver SegurityConfig).
 * Devolve a URL publica que deve ser guardada em produto.imagemProduto.
 */
@RestController
@RequestMapping("/admin/imagens")
public class ImagemController {

    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5MB
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload.diretorio:./uploads}")
    private String diretorioUpload;

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return erro("Nenhuma imagem foi selecionada.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            return erro("Imagem muito grande. O limite e de 5MB.");
        }

        String contentType = arquivo.getContentType() == null ? "" : arquivo.getContentType();
        if (!contentType.startsWith("image/")) {
            return erro("O arquivo enviado nao e uma imagem. Use JPG, PNG, WEBP ou GIF.");
        }

        String nomeOriginal = arquivo.getOriginalFilename() == null ? "imagem" : arquivo.getOriginalFilename();
        String extensao = extrairExtensao(nomeOriginal);
        if (extensao.isEmpty() || !EXTENSOES_PERMITIDAS.contains(extensao)) {
            return erro("Formato de imagem nao suportado. Use JPG, PNG, WEBP ou GIF.");
        }

        try {
            Path diretorio = Paths.get(diretorioUpload).toAbsolutePath().normalize();
            Files.createDirectories(diretorio);

            String nomeArquivo = UUID.randomUUID() + "." + extensao;
            Path destino = diretorio.resolve(nomeArquivo).normalize();
            arquivo.transferTo(destino);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("url", "/uploads/" + nomeArquivo, "nome", nomeArquivo));
        } catch (IOException e) {
            return erro("Nao foi possivel salvar a imagem no servidor: " + e.getMessage());
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf('.');
        if (ponto < 0 || ponto == nomeArquivo.length() - 1) {
            return "";
        }
        return nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }

    private ResponseEntity<Map<String, String>> erro(String mensagem) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }
}
