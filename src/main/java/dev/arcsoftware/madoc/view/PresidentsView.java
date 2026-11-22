package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.model.entity.PresidentEntity;
import dev.arcsoftware.madoc.service.PresidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Year;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/presidents")
public class PresidentsView {

    private final PresidentService presidentService;

    @Autowired
    public PresidentsView(PresidentService presidentService) {
        this.presidentService = presidentService;
    }

    @GetMapping("")
    public String getPresidents(Model model) {
        List<PresidentEntity> presidents = presidentService.getAllPresidents();

        PresidentEntity currentPresident = presidents.stream()
                .filter(PresidentEntity::isCurrent)
                .findFirst()
                .orElse(null);

        List<PresidentEntity> pastPresidents = currentPresident == null
                ? presidents
                : presidents.stream()
                        .filter(p -> p.getNumber() != currentPresident.getNumber())
                        .toList();

        PresidentEntity longestServing = presidents.stream()
                .max(Comparator.comparingInt(PresidentEntity::getYearsServed)
                        .thenComparingInt(PresidentEntity::getNumber))
                .orElse(null);

        Integer foundingYear = presidents.stream()
                .mapToInt(PresidentEntity::getStartYear)
                .min()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);

        Integer leagueYears = foundingYear != null
                ? Math.max(0, Year.now().getValue() - foundingYear)
                : null;

        model.addAttribute("presidents", presidents);
        model.addAttribute("currentPresident", currentPresident);
        model.addAttribute("pastPresidents", pastPresidents);
        model.addAttribute("longestServing", longestServing);
        model.addAttribute("foundingYear", foundingYear);
        model.addAttribute("leagueYears", leagueYears);
        return "presidents-modern";
    }
}
