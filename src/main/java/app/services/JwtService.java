package app.services;

import app.enums.ErrorMessage;
import app.exceptions.UnauthorizedException;
import app.models.Token;
import app.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    private final RedisService redisService;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        val claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return Optional.of(bearerToken.substring(7));
        }

        throw new UnauthorizedException("Missing or invalid authorization header.");
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
            log.warn(e.getMessage());
            throw new UnauthorizedException(ErrorMessage.INVALID_JWT_SIGNATURE.getMsg());
        } catch (MalformedJwtException e) {
            log.warn(e.getMessage());
            throw new UnauthorizedException(ErrorMessage.INVALID_JWT_TOKEN.getMsg());
        } catch (ExpiredJwtException e) {
            log.warn(e.getMessage());
            throw new UnauthorizedException(ErrorMessage.EXPIRED_JWT_TOKEN.getMsg());
        } catch (UnsupportedJwtException e) {
            log.warn(e.getMessage());
            throw new UnauthorizedException(ErrorMessage.UNSUPPORTED_JWT_TOKEN.getMsg());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            throw new UnauthorizedException(ErrorMessage.JWT_TOKEN_IS_NULL_OR_EMPTY.getMsg());
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .userId(user.getId())
                .token(jwtToken)
                .expired("0")
                .revoked("0")
                .build();

        redisService.saveToken(token);
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = redisService.findUserValidTokens(user.getId());
        if (validUserTokens.isEmpty())
            return;

        var revokedTokens = validUserTokens.stream()
                .map(t -> t.withExpired("1"))
                .map(t -> t.withRevoked("1"))
                .toList();

        redisService.saveAllTokens(revokedTokens);
    }
}
