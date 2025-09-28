package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.auth.model.AuthToken;
import dev.arcsoftware.madoc.auth.model.AuthenticationRequest;
import dev.arcsoftware.madoc.auth.model.ChangePasswordRequest;
import dev.arcsoftware.madoc.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/auth")
public class AuthorizationController {

    @Value("${auth.cookie.name}")
    private String cookieName;

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthorizationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Boolean> login(
            HttpServletResponse response,
            @RequestParam Map<String, String> paramMap
    ){
        AuthenticationRequest authRequest = new AuthenticationRequest(paramMap.get("username"), paramMap.get("password"));
        AuthToken authToken = authenticationService.authenticate(authRequest);
        addAuthCookie(response, authToken);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/logout")
    public ResponseEntity<Boolean> logout(
            HttpServletResponse response
    ){
        addAuthCookie(response, authenticationService.emptyToken());
        return ResponseEntity.ok(true);
    }

    @PutMapping(path = "/manage/password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Boolean> changePassword(
            HttpServletResponse response,
            @RequestParam Map<String, String> paramMap
    ){
        ChangePasswordRequest authRequest = new ChangePasswordRequest(
                Objects.requireNonNull(paramMap.get("username")),
                Objects.requireNonNull(paramMap.get("oldPassword")),
                Objects.requireNonNull(paramMap.get("newPassword"))
        );

        AuthToken authToken = authenticationService.changePassword(authRequest);
        addAuthCookie(response, authToken);
        log.info("Password changed for user '{}'", authRequest.username());
        return ResponseEntity.ok(true);
    }

    private void addAuthCookie(HttpServletResponse response, AuthToken token){
        Cookie authCookie = new Cookie(cookieName, token.jwt());
        authCookie.setMaxAge(token.expiryInSeconds());
        authCookie.setPath(token.validityPath());
        authCookie.setSecure(true);
        authCookie.setHttpOnly(true);
        // Add the cookie to the response
        response.addCookie(authCookie);
    }
}
