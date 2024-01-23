package app.services;

import app.models.Token;
import app.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final TokenRepository tokenRepository;

    public Optional<Token> findToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    public void saveAllTokens(List<Token> tokens) {
        tokenRepository.saveAll(tokens);
    }

    public List<Token> findUserValidTokens(String userId) {
        return tokenRepository.findByUserIdAndRevokedAndExpired(userId, "0", "0");
    }

    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }
}
