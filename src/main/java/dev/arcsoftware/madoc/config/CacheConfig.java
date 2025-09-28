package dev.arcsoftware.madoc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@EnableCaching
@Configuration
public class CacheConfig {

    public static final String CACHE_MANAGER = "cacheManager";

    public static final String RULES_CACHE = "rules";
    public static final String NEWS_CACHE = "news";
    public static final String LATEST_NEWS_CACHE = "latest_news";
    public static final String SEASON_METADATA_YEAR_CACHE = "season-metadata-year";
    public static final String SEASON_METADATA_TYPE_CACHE = "season-metadata-season-type";
    public static final String SEASON_METADATA_CACHE = "season-metadata";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(
                List.of(
                        RULES_CACHE,
                        NEWS_CACHE,
                        LATEST_NEWS_CACHE,
                        SEASON_METADATA_YEAR_CACHE,
                        SEASON_METADATA_TYPE_CACHE,
                        SEASON_METADATA_CACHE
                )
        );
        return cacheManager;
    }
}
