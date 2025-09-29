package dev.arcsoftware.madoc.auth.filter;

import dev.arcsoftware.madoc.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class JwtAuthFilter implements Filter {

    private final JwtConfig jwtConfig;

    public JwtAuthFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if(request.getRequestURI().endsWith("/login") || request.getRequestURI().endsWith("/logout")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String token = extractJwtFromRequest(request);

        if (token != null) {
            log.debug("Attempting to authenticate using JWT Token");
            setSecurityContext(token);
        }


        log.debug("Successfully authenticated using jwt token");
        filterChain.doFilter(servletRequest, servletResponse);
    }



    private void setSecurityContext(String token) {
        try {	// exceptions might be thrown in creating the claims if for example the token is expired
            // 4. Validate the token
            Claims claims = Jwts.parser()
                    .verifyWith(jwtConfig.getSecretKey()).build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            if(username != null) {
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");

                List<String> combinedAuthorities = Stream.concat(
                        Optional.ofNullable(authorities).orElse(new ArrayList<>()).stream(),
                        Optional.ofNullable(roles).stream().map(role -> "ROLE_" + role)
                ).toList();

                // 5. Create auth object
                // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, combinedAuthorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                // 6. Authenticate the user
                // Now, user is authenticated
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            log.error("Unable to get Authentication from JWT Token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        return Stream.of(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> "jwt-auth".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
