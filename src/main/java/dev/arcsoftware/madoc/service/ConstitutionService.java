package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.repository.RuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.arcsoftware.madoc.config.CacheConfig.CACHE_MANAGER;
import static dev.arcsoftware.madoc.config.CacheConfig.RULES_CACHE;

@Slf4j
@Service
public class ConstitutionService {

    private final RuleRepository ruleRepository;

    @Autowired
    public ConstitutionService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value = RULES_CACHE)
    public List<RuleEntity> getRules() {
        log.info("cache miss for rules: calling repository");
        return ruleRepository.getAllRules();
    }

    public void updateRules(List<RuleEntity> rules) {
        log.info("Updating {} rules: calling repository", rules.size());

        for(var rule : rules) {
            this.ruleRepository.updateRule(rule);
        }
    }
}
