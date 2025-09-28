package dev.arcsoftware.madoc.config;

import dev.arcsoftware.madoc.auth.filter.AuthCookieFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfig {

    @Bean
    public FilterRegistrationBean<AuthCookieFilter> authCookieFilter() {
        FilterRegistrationBean<AuthCookieFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthCookieFilter());
        registrationBean.addUrlPatterns("/api/*", "/admin", "/admin/*"); // Apply to all api URLs and admin pages
        registrationBean.setOrder(1); // Set order if multiple filters are present
        return registrationBean;
    }
}
