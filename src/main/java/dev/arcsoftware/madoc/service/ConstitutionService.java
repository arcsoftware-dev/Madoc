package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.payload.RuleDto;
import dev.arcsoftware.madoc.repository.RuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ConstitutionService {

    private final RuleRepository ruleRepository;

    @Autowired
    public ConstitutionService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }


    public List<RuleDto> getRules() {
        return ruleRepository.getAllRules();
    }
}
