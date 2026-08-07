package coretech.sistemaCoreTech.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    private final Key chave;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        byte[] chaveBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.chave = new SecretKeySpec(chaveBytes, "HmacSHA256");
    }

    public String gerarToken(String email, Long id) {
        Date agora = new Date();
        return Jwts.builder()
                .setSubject(email)
                .claim("id", id)
                .setIssuedAt(agora)
                .setExpiration(new Date(agora.getTime() + 86400000L)) // 24h
                .signWith(chave, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairId(String token) {
        Object id = extrairClaims(token).get("id");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return Long.valueOf(id.toString());
    }

    public boolean validarToken(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
