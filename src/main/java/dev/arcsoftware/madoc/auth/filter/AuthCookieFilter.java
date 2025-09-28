package dev.arcsoftware.madoc.auth.filter;

import dev.arcsoftware.madoc.exception.UnauthorizedException;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class AuthCookieFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);

        String jwtAuth = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("jwt-auth"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if(jwtAuth == null){
            throw new UnauthorizedException("Unauthorized.  Invalid JWT token.");
        }

        log.info("Successfully authenticated using jwt token: {}", jwtAuth);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
