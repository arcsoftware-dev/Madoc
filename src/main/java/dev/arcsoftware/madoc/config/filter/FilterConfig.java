package dev.arcsoftware.madoc.config.filter;

import dev.arcsoftware.madoc.repository.HomeViewCountRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<HomeViewCountFilter> homeViewCountFilterFilterRegistrationBean(HomeViewCountRepository homeViewCountRepository){
        FilterRegistrationBean<HomeViewCountFilter> registrationBean = new FilterRegistrationBean<>();
        HomeViewCountFilter filter = new HomeViewCountFilter(homeViewCountRepository);
        registrationBean.setFilter(filter);
        registrationBean.setUrlPatterns(Set.of("/"));
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
