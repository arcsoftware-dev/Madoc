package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.auth.model.AuthToken;
import dev.arcsoftware.madoc.auth.model.AuthenticationRequest;
import dev.arcsoftware.madoc.auth.model.ChangePasswordRequest;
import dev.arcsoftware.madoc.auth.model.UserEntity;
import dev.arcsoftware.madoc.config.JwtConfig;
import dev.arcsoftware.madoc.enums.Role;
import dev.arcsoftware.madoc.exception.UnauthorizedException;
import dev.arcsoftware.madoc.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationService {

    @Value("${auth.cookie.expiry_seconds}")
    private int cookieExpiryInSeconds;

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(JwtConfig jwtConfig, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtConfig = jwtConfig;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void ensureAdminExists(){
        Optional<UserEntity> adminUser = userRepository.findUserByUsername("admin");
        if(adminUser.isEmpty()){
            String defaultPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            log.info("Admin user not found, created with default password '{}'. Please change this password immediately!", defaultPassword);
            UserEntity user = new UserEntity();
            user.setUsername("admin");
            user.setPasswordHash(passwordEncoder.encode(defaultPassword));
            user.setRoles(Collections.singletonList(Role.ADMIN));

            userRepository.insertUser(user);
        }
    }

    public AuthToken authenticate(AuthenticationRequest authRequest){
        Optional<UserEntity> userOpt = userRepository.findUserByUsername(authRequest.username());

        if(userOpt.isEmpty() || !matchingPasswordHash(authRequest.password(), userOpt.get().getPasswordHash())){
            throw new UnauthorizedException("Unauthorized, invalid username or password");
        }

        String token = generateJwtToken(userOpt.get());
        return new AuthToken(cookieExpiryInSeconds, token);
    }

    private boolean matchingPasswordHash(String passwordReq, String passwordHash) {
        return passwordEncoder.matches(passwordReq, passwordHash);
    }

    public AuthToken emptyToken(){
        return new AuthToken(0, "");
    }

    private String generateJwtToken(UserEntity user){
        return Jwts.builder()
                .issuer("madoc")
                .subject(user.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(jwtConfig.getExpirySeconds())))
                .claim("roles", Optional.ofNullable(user.getRoles()).orElse(Collections.emptyList()))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public AuthToken changePassword(ChangePasswordRequest authRequest) {
        Optional<UserEntity> userOpt = userRepository.findUserByUsername(authRequest.username());

        if(userOpt.isEmpty() || !matchingPasswordHash(authRequest.oldPassword(), userOpt.get().getPasswordHash())){
            throw new UnauthorizedException("Unauthorized, invalid username or password");
        }

        UserEntity user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(authRequest.newPassword()));

        userRepository.updateUserPasswordHash(user);

        String token = generateJwtToken(user);
        return new AuthToken(cookieExpiryInSeconds, token);

    }
}
