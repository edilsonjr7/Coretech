package coretech.sistemaCoreTech.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SegurityConfig {


    // aqui criptografa a senha   
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // vai criptografar me 60 caracteres igual na entidade do usuario
    }

// org.springframework.http.HttpMethod.PUT,
                    //org.springframework.http.HttpMethod.DELETE,

  
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // rota de login e cadastro
                .requestMatchers("/auth/**").permitAll()

                // apenas em ambiente de teste
                .requestMatchers("/h2-console/**").permitAll()

                // rotas exclusivas do ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET, "/produtos/**"
                ).permitAll()
                .requestMatchers(
                    org.springframework.http.HttpMethod.POST,"/produtos/**"
                ).hasRole("ADMIN") 
                .requestMatchers(
                    org.springframework.http.HttpMethod.PUT,"/produtos/**"
                ).hasRole("ADMIN") 
                .requestMatchers(
                    org.springframework.http.HttpMethod.DELETE,"/produtos/**"
                ).hasRole("ADMIN") 

                // rotas exclusivas do usario autenticado
                .requestMatchers("/carrinho/**").hasRole("USER")
                .requestMatchers("/favoritos/**").hasRole("USER")

                // qualquer outra rota exige autenticação
                .anyRequest().authenticated()
            )


            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}

    
    

