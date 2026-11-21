package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.model.payload.PlayerGameLogPage;
import dev.arcsoftware.madoc.model.payload.PlayerProfileDto;
import dev.arcsoftware.madoc.service.PlayersService;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping(value = "/{playerId}/game-logs", produces = "text/html;charset=UTF-8")
    public String getPlayerGameLogs(
            @PathVariable("playerId") int playerId,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "5") int limit,
            @RequestParam(value = "goalie", defaultValue = "false") boolean goalie,
            Model model,
            HttpServletResponse response
    ) {
        log.info("Fetching game logs for playerId={}, offset={}, limit={}", playerId, offset, limit);
        PlayerGameLogPage page = playersService.getPlayerGameLogs(playerId, offset, limit);
        int nextOffset = page.getOffset() + page.getGames().size();
        model.addAttribute("games", page.getGames());
        model.addAttribute("isGoalie", goalie);
        model.addAttribute("hasMore", page.hasMore());
        model.addAttribute("nextOffset", nextOffset);
        model.addAttribute("totalGames", page.getTotalGames());
        model.addAttribute("remaining", page.getRemaining());

        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Has-More", String.valueOf(page.hasMore()));
        response.setHeader("X-Next-Offset", String.valueOf(nextOffset));
        response.setHeader("X-Total-Games", String.valueOf(page.getTotalGames()));
        response.setHeader("X-Remaining", String.valueOf(page.getRemaining()));
        return "fragments/player-tables :: gameLogPage";
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
