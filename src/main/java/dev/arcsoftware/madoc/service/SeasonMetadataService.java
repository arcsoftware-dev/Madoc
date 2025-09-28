package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.repository.SeasonMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static dev.arcsoftware.madoc.config.CacheConfig.*;

@Slf4j
@Service
public class SeasonMetadataService {

    private final SeasonMetadataRepository seasonMetadataRepository;

    @Autowired
    public SeasonMetadataService(SeasonMetadataRepository seasonMetadataRepository) {
        this.seasonMetadataRepository = seasonMetadataRepository;
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value=SEASON_METADATA_CACHE)
    public Pair<Integer, SeasonType> normalizeSeasonData(Integer year, SeasonType seasonType) {
        log.info("cache-miss for season metadata normalization {} {}: calling repository", year, seasonType);
        Integer normalizedYear = year;
        SeasonType normalizedSeasonType = seasonType;

        if (normalizedSeasonType == null) {
            normalizedSeasonType = this.getCurrentSeasonType();
        }
        if (normalizedYear == null) {
            normalizedYear = this.getCurrentSeasonYear();
        }
        log.debug("Parameters after normalization. year: {}, seasonType: {}", normalizedYear, normalizedSeasonType);
        return new Pair<>(normalizedYear, normalizedSeasonType);
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value=SEASON_METADATA_TYPE_CACHE)
    public SeasonType getCurrentSeasonType(){
        log.info("cache-miss for season metadata season type: calling repository");
        return seasonMetadataRepository.getCurrentSeasonType()
                .orElseThrow(() -> new IllegalStateException("No current season type found in database"));
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value=SEASON_METADATA_YEAR_CACHE)
    public Integer getCurrentSeasonYear(){
        log.info("cache-miss for season metadata year: calling repository");
        return seasonMetadataRepository.getCurrentYear()
                .orElseThrow(() -> new IllegalStateException("No current season year found in database"));
    }
}
