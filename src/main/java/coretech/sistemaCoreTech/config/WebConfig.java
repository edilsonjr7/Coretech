package coretech.sistemaCoreTech.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serve as imagens enviadas pelos administradores (pasta configurada em
 * app.upload.diretorio, padrão ./uploads) na URL publica /uploads/**.
 * Sem isso, arquivos criados em tempo de execucao nao seriam encontrados
 * porque o Spring so serve automaticamente o que esta em classpath:/static.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.diretorio:./uploads}")
    private String diretorioUpload;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(diretorioUpload).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
