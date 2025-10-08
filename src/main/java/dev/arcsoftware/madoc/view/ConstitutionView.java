package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.ConstitutionController;
import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.model.payload.GamesheetPayload;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/constitution")
public class ConstitutionView {

    private final ConstitutionController constitutionController;

    @Autowired
    public ConstitutionView(ConstitutionController constitutionController) {
        this.constitutionController = constitutionController;
    }

    @ModelAttribute("rules")
    public List<RuleEntity> getRules() {
        return constitutionController.getAllRules().getBody();
    }

    @GetMapping("")
    public String getConstitution(Model model) {
        return "constitution";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]')")
    @RequestMapping(value = "/replace", params = {"replace"})
    public String replaceRules(
            @RequestParam("file") MultipartFile constitutionFile
    ) throws IOException {
        log.info("Replacing Ruleset with file {}", constitutionFile.getOriginalFilename());
        constitutionController.replaceRules(constitutionFile);

        return "redirect:/constitution";
    }
}
