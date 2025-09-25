package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.ConstitutionController;
import dev.arcsoftware.madoc.model.entity.RuleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("")
    public String getConstitution(Model model) {
        List<RuleEntity> rules = constitutionController.getAllRules().getBody();
        model.addAttribute("rules", rules);
        return "constitution";
    }
}
