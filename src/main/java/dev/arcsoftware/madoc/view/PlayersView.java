package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.model.payload.PlayerProfileDto;
import dev.arcsoftware.madoc.service.PlayersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/players")
public class PlayersView {

    private final PlayersService playersService;

    @Autowired
    public PlayersView(PlayersService playersService) {
        this.playersService = playersService;
    }

    @GetMapping("/{playerId}")
    public String getPlayerProfile(
            @PathVariable("playerId") int playerId,
            Model model
    ) {
        log.info("Fetching player profile for playerId={}", playerId);
        PlayerProfileDto profile = playersService.getPlayerProfile(playerId);
        model.addAttribute("player", profile);
        return "player-modern";
    }

    @GetMapping("/lookup")
    public String lookupPlayerByName(
            @RequestParam("name") String name,
            Model model
    ) {
        log.info("Looking up player profile by name={}", name);
        Optional<Integer> playerId = playersService.findPlayerIdByName(name);
        if (playerId.isPresent()) {
            return "redirect:/players/" + playerId.get();
        }

        PlayerProfileDto profile = playersService.getHistoricalPlayerProfileByName(name);
        model.addAttribute("player", profile);
        return "player-modern";
    }
}
