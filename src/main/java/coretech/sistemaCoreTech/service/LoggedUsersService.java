package coretech.sistemaCoreTech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoggedUsersService {

    // token -> email (mantém usuários logados em memória)
    private final Map<String, String> loggedTokens = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> loginTimes = new ConcurrentHashMap<>();

    public void registrarLogin(String token, String email) {
        loggedTokens.put(token, email);
        loginTimes.put(token, LocalDateTime.now());
    }

    public void registrarLogout(String token) {
        loggedTokens.remove(token);
        loginTimes.remove(token);
    }

    public boolean isLogado(String token) {
        return loggedTokens.containsKey(token);
    }

    public List<Map<String, String>> listarLogados() {
        List<Map<String, String>> resultado = new ArrayList<>();
        loggedTokens.forEach((token, email) -> {
            Map<String, String> item = new java.util.HashMap<>();
            item.put("email", email);
            item.put("loginEm", loginTimes.getOrDefault(token, LocalDateTime.now()).toString());
            resultado.add(item);
        });
        return resultado;
    }
}
