package app.repository;

import app.models.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, String> {

    Optional<Token> findByToken(String token);

    List<Token> findByUserIdAndRevokedAndExpired(String userId, String revoked, String expired);

    void deleteByToken(String token);

}
