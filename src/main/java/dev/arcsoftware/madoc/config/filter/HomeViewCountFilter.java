package dev.arcsoftware.madoc.config.filter;

import dev.arcsoftware.madoc.repository.HomeViewCountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class HomeViewCountFilter extends OncePerRequestFilter {
    private final HomeViewCountRepository repository;

    public HomeViewCountFilter(HomeViewCountRepository repository) {
        this.repository = repository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long viewCount = repository.getNextViewCount();
        log.info("Count Filter triggered: {}", viewCount);

        filterChain.doFilter(request, response);
    }
}
