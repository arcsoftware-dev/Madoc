package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.repository.RuleRepository;
import dev.arcsoftware.madoc.util.FileUploadParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ConstitutionService {

    private final RuleRepository ruleRepository;
    private final FileUploadParser fileUploadParser;


    @Autowired
    public ConstitutionService(RuleRepository ruleRepository, FileUploadParser fileUploadParser) {
        this.ruleRepository = ruleRepository;
        this.fileUploadParser = fileUploadParser;
    }

    public List<RuleEntity> getRules() {
        log.info("cache miss for rules: calling repository");
        return ruleRepository.getAllRules();
    }

    public void updateRules(List<RuleEntity> rules) {
        log.info("Updating {} rules and evicting rules cache", rules.size());
        for(var rule : rules) {
            this.ruleRepository.updateRule(rule);
        }
    }

    @Transactional
    public List<RuleEntity> uploadNewRuleSet(MultipartFile constitutionFile) throws IOException {
        List<RuleEntity> newRules = fileUploadParser.parseRuleFile(constitutionFile.getBytes());
        if(newRules.isEmpty()) {
            throw new IllegalArgumentException("Parsed File contains no rules");
        }

        ruleRepository.deleteAll();
        ruleRepository.insertNewRules(newRules);

        return newRules;
    }
}
