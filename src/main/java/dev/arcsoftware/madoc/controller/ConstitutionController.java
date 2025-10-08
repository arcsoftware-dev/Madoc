package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.service.ConstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/constitution")
public class ConstitutionController {
    private final ConstitutionService constitutionService;

    @Autowired
    public ConstitutionController(ConstitutionService constitutionService) {
        this.constitutionService = constitutionService;
    }

    @GetMapping("")
    public ResponseEntity<List<RuleEntity>> getAllRules() {
        log.info("Fetching all constitution rules");
        List<RuleEntity> rules = constitutionService.getRules();
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]')")
    @PostMapping(value = "/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<RuleEntity>> replaceRules(
            @RequestParam("file") MultipartFile constitutionFile
    ) throws IOException {
        log.info("Replacing all constitution rules");
        List<RuleEntity> rules = constitutionService.uploadNewRuleSet(constitutionFile);
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }
}
