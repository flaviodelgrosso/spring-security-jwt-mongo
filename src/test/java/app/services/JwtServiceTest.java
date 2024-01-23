package app.services;

import app.exceptions.UnauthorizedException;
import app.models.Token;
import app.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtServiceTest {

    @Mock
    private RedisService redisService;

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetails;

    private String jwtToken;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userDetails = new org.springframework.security.core.userdetails.User(
                "testuser",
                "password",
                new java.util.ArrayList<>());

        jwtToken = jwtService.generateToken(userDetails);

    }

    @Test
    public void testExtractUsername() {
        String username = jwtService.extractUsername(jwtToken);
        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    public void testExtractClaim() {
        Claims claims = new DefaultClaims();
        claims.setSubject(userDetails.getUsername());

        String claimValue = jwtService.extractClaim(jwtToken, Claims::getSubject);
        assertEquals(claims.getSubject(), claimValue);
    }

    @Test
    public void testGenerateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
    }

    @Test
    public void testGenerateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("testClaim", "testValue");

        String token = jwtService.generateToken(extraClaims, userDetails);
        assertNotNull(token);
    }

    @Test
    public void testGetJwtFromRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);

        Optional<String> jwt = jwtService.getJwtFromRequest(request);
        assertTrue(jwt.isPresent());
        assertEquals(jwtToken, jwt.get());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetJwtFromRequestMissingHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtService.getJwtFromRequest(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetJwtFromRequestInvalidHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        jwtService.getJwtFromRequest(request);
    }

    @Test
    public void testIsTokenValid() {
        boolean isValid = jwtService.isTokenValid(jwtToken, userDetails);
        assertTrue(isValid);
    }

    @Test
    public void testSaveUserToken() {
        User user = new User().withId("testId");

        jwtService.saveUserToken(user, jwtToken);

        verify(redisService, times(1)).saveToken(any(Token.class));
    }

    @Test
    public void testRevokeAllUserTokens() {
        User user = new User().withId("testId");

        Token token = Token.builder()
                .userId(user.getId())
                .token(jwtToken)
                .expired("0")
                .revoked("0")
                .build();

        when(redisService.findUserValidTokens(user.getId())).thenReturn(java.util.List.of(token));

        jwtService.revokeAllUserTokens(user);

        verify(redisService, times(1)).saveAllTokens(any(java.util.List.class));
    }
}