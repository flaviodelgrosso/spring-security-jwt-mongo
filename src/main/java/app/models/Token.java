package app.models;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@With
@Getter
@Builder
@RedisHash("Token")
public class Token {

    @Id
    private String id;

    @Indexed
    private String token;
    @Indexed
    private String revoked;
    @Indexed
    private String expired;

    @Indexed
    private String userId;
}
