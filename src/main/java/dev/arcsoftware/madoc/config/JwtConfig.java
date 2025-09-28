package dev.arcsoftware.madoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@ConfigurationProperties("auth.jwt")
@Setter
public class JwtConfig {
    private String secret;
    @Getter
    private int expirySeconds;

    public SecretKey getSecretKey() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        return new SecretKeySpec(secretBytes, "HmacSHA512");
    }
}
