package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.auth.model.AuthToken;
import dev.arcsoftware.madoc.auth.model.AuthenticationRequest;
import dev.arcsoftware.madoc.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(
            HttpServletResponse response,
            @RequestBody AuthenticationRequest authRequest
    ){
        AuthToken authToken = authenticationService.authenticate(authRequest);
        addAuthCookie(response, authToken);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(
            HttpServletResponse response
    ){
        addAuthCookie(response, authenticationService.emptyToken());
        return ResponseEntity.ok(true);
    }

    private void addAuthCookie(HttpServletResponse response, AuthToken token){
        // Create a new Cookie
        Cookie authCookie = new Cookie(cookieName, token.jwt());
        authCookie.setMaxAge(token.expiryInSeconds()); // Set cookie to expire in 1 hour (in seconds)
        authCookie.setPath(token.validityPath());
        authCookie.setSecure(true);
        authCookie.setHttpOnly(true);
        // Add the cookie to the response
        response.addCookie(authCookie);
    }
}
