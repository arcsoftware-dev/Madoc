package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.GameController;
import dev.arcsoftware.madoc.controller.RosterController;
import dev.arcsoftware.madoc.controller.StandingsController;
import dev.arcsoftware.madoc.enums.GoalType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.GameEntity;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.payload.*;
import dev.arcsoftware.madoc.service.GameService;
import dev.arcsoftware.madoc.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/games")
public class GamesheetView {

    private final GameController gameController;
    private final RosterController rosterController;
    private final GameService gameService;
    private final StandingsController standingsController;

    @Autowired
    public GamesheetView(GameController gameController, RosterController rosterController, GameService gameService, StandingsController standingsController) {
        this.gameController = gameController;
        this.rosterController = rosterController;
        this.gameService = gameService;
        this.standingsController = standingsController;
    }

    @ModelAttribute("goalTypes")
    public List<String> goalTypes() {
        return Arrays.stream(GoalType.values())
                .map(Enum::name)
                .toList();
    }

    private void addRostersToModel(Model model, String homeTeam, String awayTeam, int year) {
        var homeRoster = Objects.requireNonNull(rosterController.getRosterAssignmentsByTeam(year, homeTeam).getBody()).stream().filter(RosterAssignment::isActive).toList();
        var awayRoster = Objects.requireNonNull(rosterController.getRosterAssignmentsByTeam(year, awayTeam).getBody()).stream().filter(RosterAssignment::isActive).toList();
        model.addAttribute("homeTeamPlayers", homeRoster);
        model.addAttribute("awayTeamPlayers", awayRoster);
    }

    @GetMapping("/{gameId}")
    public String getGamesheet(
            @PathVariable("gameId") int gameId,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.info("Unauthenticated user attempted to access gameSheet for game {}, redirecting to matchup view", gameId);
            return "redirect:/games/matchup/" + gameId;
        }

        log.info("Fetching gameSheet for game {}", gameId);
        GamesheetPayload gameSheet = gameController.fetchGamesheet(gameId).getBody();

        model.addAttribute("gameSheet", gameSheet);
        assert gameSheet != null;
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());

        return "gameSheet";
    }

    @GetMapping("/matchup/{gameId}")
    public String getMatchup(
            @PathVariable("gameId") int gameId,
            Model model
    ) {
        GameEntity gameEntity = gameService.fetchGameEntityById(gameId);
        model.addAttribute("game", gameEntity);

        List<GameSummary> previousGames = gameService.fetchPreviousGamesBetweenTeams(gameEntity.getHomeTeam().getTeamName(), gameEntity.getAwayTeam().getTeamName(), gameEntity.getYear());
        model.addAttribute("previousGames", previousGames);

        MatchupSummary regularSeasonMatchupSummary = gameService.createMatchupSummaryFromGames(previousGames, gameEntity.getHomeTeam().getTeamName(), gameEntity.getAwayTeam().getTeamName(), SeasonType.REGULAR_SEASON);
        model.addAttribute("regSeasonHeadToHead", regularSeasonMatchupSummary);

        MatchupSummary playoffMatchupSummary = gameService.createMatchupSummaryFromGames(previousGames, gameEntity.getHomeTeam().getTeamName(), gameEntity.getAwayTeam().getTeamName(), SeasonType.PLAYOFFS);
        model.addAttribute("playoffHeadToHead", playoffMatchupSummary);

        var seasonStandings = Objects.requireNonNull(standingsController.getStandings(gameEntity.getYear(), SeasonType.REGULAR_SEASON, null, null).getBody())
                .stream()
                .filter(teamStatsDto -> teamStatsDto.getTeamName().equals(gameEntity.getHomeTeam().getTeamName()) || teamStatsDto.getTeamName().equals(gameEntity.getAwayTeam().getTeamName()))
                .map(dto -> StandingMatchupSummary.builder()
                        .team(dto.getTeamName())
                        .gamesPlayed(dto.getGamesPlayed())
                        .wins(dto.getWins())
                        .losses(dto.getLosses())
                        .ties(dto.getTies())
                        .goalsFor(dto.getGoalsFor())
                        .goalsAgainst(dto.getGoalsAgainst())
                        .penaltyMinutes(dto.getPenaltyMinutes())
                        .build()
                )
                .collect(Collectors.toMap(StandingMatchupSummary::getTeam, Function.identity()));
        model.addAttribute("seasonStandings", seasonStandings);

        var playoffStandings = Objects.requireNonNull(standingsController.getStandings(gameEntity.getYear(), SeasonType.PLAYOFFS, null, null).getBody())
                .stream()
                .filter(teamStatsDto -> teamStatsDto.getTeamName().equals(gameEntity.getHomeTeam().getTeamName()) || teamStatsDto.getTeamName().equals(gameEntity.getAwayTeam().getTeamName()))
                .map(dto -> StandingMatchupSummary.builder()
                        .team(dto.getTeamName())
                        .gamesPlayed(dto.getGamesPlayed())
                        .wins(dto.getWins())
                        .losses(dto.getLosses())
                        .ties(dto.getTies())
                        .goalsFor(dto.getGoalsFor())
                        .goalsAgainst(dto.getGoalsAgainst())
                        .penaltyMinutes(dto.getPenaltyMinutes())
                        .build()
                )
                .collect(Collectors.toMap(StandingMatchupSummary::getTeam, Function.identity()));
        model.addAttribute("playoffStandings", playoffStandings);

        return "matchup";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"addGoal"})
    public String addGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") GamesheetPayload gameSheet,
            final BindingResult bindingResult,
            Model model) {
        log.info("Adding {} goal to gameSheet {}", gameSheet.getNextGoal().getTeam(), gameId);
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());

        var errors = getNextGoalErrors(gameSheet.getNextGoal(),
                gameSheet.getNextGoal().getTeam().equals(gameSheet.getHomeTeam())
                        ? gameSheet.getHomeAttendanceByPlayerId()
                        : gameSheet.getAwayAttendanceByPlayerId()
        );

        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gameSheet";
        }

        if(gameSheet.getNextGoal().getTeam().equals(gameSheet.getHomeTeam())){
            gameSheet.addHomeGoal(gameSheet.getNextGoal().getGoal());
        }
        else{
            gameSheet.addAwayGoal(gameSheet.getNextGoal().getGoal());
        }

        clearNextData(gameSheet, false);

        gameController.updateGamesheet(gameSheet);
        return "gameSheet";
    }

    private List<ObjectError> getNextGoalErrors(NextGoalPayload nextGoal, List<AttendancePayload> attendance) {
        List<ObjectError> errors = new ArrayList<>();
        if(nextGoal == null){
            errors.add(new ObjectError("Add Goal", "Next Goal: Data is Null"));
            return errors;
        }
        if(Utils.isNullOrEmpty(nextGoal.getTeam())){
            errors.add(new ObjectError("Add Goal", "Next Goal: Team is Empty"));
        }

        if(nextGoal.getGoal() == null){
            errors.add(new ObjectError("Add Goal", "Next Goal: Goal Data is Null"));
            return errors;
        }

        errors.addAll(getGoalPayloadErrors(List.of(nextGoal.getGoal()), attendance, nextGoal.getTeam(), true));

        return errors;
    }

    private List<ObjectError> getGoalPayloadErrors(List<GoalPayload> goals, List<AttendancePayload> attendance, String teamName, boolean isNew) {
        List<ObjectError> errors = new ArrayList<>();

        if(goals == null){return errors;}

        for(int i=0; i<goals.size(); i++){
            GoalPayload goalPayload = goals.get(i);
            String errorMessagePrefix;
            if(isNew){
                errorMessagePrefix = teamName + " New Goal: ";
            }
            else{
                errorMessagePrefix = teamName + " Goal " + (i+1) + ": ";
            }

            if(goalPayload.getPeriod() == null || goalPayload.getPeriod() < 1 || goalPayload.getPeriod() > 4){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Period Must Be Between 1 And 4 Inclusive"));
            }

            Pattern timePattern = Pattern.compile("^[0-5]?\\d:[0-5]\\d$");
            boolean validTimeFormat = timePattern.matcher(goalPayload.getTime()).matches();
            if(validTimeFormat){
                LocalTime goalTime = null;
                try{
                    goalTime = gameService.timeStringToLocalTime(goalPayload.getTime());
                } catch(Exception ignored){}
                if(goalTime == null){
                    errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Time Must Be Set in the format 'mm:ss'"));
                }
            }
            else {
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Time Must Be Set in the format 'mm:ss'"));
            }

            if(goalPayload.getScorer() == null ||
                    ( attendance == null
                            || attendance.stream()
                            .filter(j -> j.getJerseyNumber().equals(goalPayload.getScorer()))
                            .map(AttendancePayload::getAttended)
                            .findFirst().orElse(Boolean.FALSE)
                            .equals(Boolean.FALSE)
                    )
            ){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Scorer Must Be Present In The Attendance And Be Playing"));
            }

            if(goalPayload.getAssist1() != null &&
                    ( attendance == null
                            || attendance.stream()
                            .filter(j -> j.getJerseyNumber().equals(goalPayload.getAssist1()))
                            .map(AttendancePayload::getAttended)
                            .findFirst().orElse(Boolean.FALSE)
                            .equals(Boolean.FALSE)
                    )
            ){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Primary Assist Player Must Be Present In The Attendance And Be Playing"));
            }

            if(goalPayload.getAssist2() != null &&
                    ( attendance == null
                            || attendance.stream()
                            .filter(j -> j.getJerseyNumber().equals(goalPayload.getAssist2()))
                            .map(AttendancePayload::getAttended)
                            .findFirst().orElse(Boolean.FALSE)
                            .equals(Boolean.FALSE)
                    )
            ){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Secondary Assist Player Must Be Present In The Attendance And Be Playing"));
            }

        }
        return errors;
    }

    private List<ObjectError> getNextPenaltyErrors(NextPenaltyPayload nextPenalty) {
        List<ObjectError> errors = new ArrayList<>();
        if(nextPenalty == null){
            errors.add(new ObjectError("Add Penalty", "Next Penalty: Data is Null"));
            return errors;
        }
        if(Utils.isNullOrEmpty(nextPenalty.getTeam())){
            errors.add(new ObjectError("Add Penalty", "Next Penalty: Team is Empty"));
        }

        if(nextPenalty.getPenalty() == null){
            errors.add(new ObjectError("Add Penalty", "Next Penalty: Penalty Data is Null"));
            return errors;
        }

        errors.addAll(getPenaltyPayloadErrors(List.of(nextPenalty.getPenalty()), nextPenalty.getTeam(), true));

        return errors;
    }

    private List<ObjectError> getPenaltyPayloadErrors(List<PenaltyPayload> penalties, String teamName, boolean isNew) {
        List<ObjectError> errors = new ArrayList<>();

        if(penalties == null){return errors;}

        for(int i=0; i<penalties.size(); i++){
            PenaltyPayload penaltyPayload = penalties.get(i);
            String errorMessagePrefix;
            if(isNew){
                errorMessagePrefix = teamName + " New Penalty: ";
            }
            else{
                errorMessagePrefix = teamName + " Penalty " + (i+1) + ": ";
            }

            if(penaltyPayload.getJerseyNumber() == null){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix + "Player Must Be Set"));
            }
            if(penaltyPayload.getMinutes() == null){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix + "Minutes Must Be Set"));
            }
            if(penaltyPayload.getInfraction() == null || penaltyPayload.getInfraction().trim().isEmpty()){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix + "Infraction Must Be Set"));
            }
            if(penaltyPayload.getPeriod() == null || penaltyPayload.getPeriod() < 1 || penaltyPayload.getPeriod() > 4){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix + "Period Must Be Between 1 And 4 Inclusive"));
            }

            Pattern timePattern = Pattern.compile("^[0-5]?\\d:[0-5]\\d$");
            boolean validTimeFormat = timePattern.matcher(penaltyPayload.getTime()).matches();
            if(validTimeFormat){
                LocalTime penaltyTime = null;
                try{
                    penaltyTime = gameService.timeStringToLocalTime(penaltyPayload.getTime());
                } catch(Exception ignored){}
                if(penaltyTime == null){
                    errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Time Must Be Set in the format 'mm:ss'"));
                }
            }
            else {
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix+"Time Must Be Set in the format 'mm:ss'"));
            }
        }
        return errors;
    }

    private void clearNextData(GamesheetPayload gameSheet, boolean fullClear) {
        if(fullClear) {
            gameSheet.setNextGoal(null);
            gameSheet.setNextPenalty(null);
        }
        else {
            if (gameSheet.getNextGoal() != null) gameSheet.getNextGoal().setGoal(null);
            if (gameSheet.getNextPenalty() != null) gameSheet.getNextPenalty().setPenalty(null);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removeGoal"})
    public String removeGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") GamesheetPayload gameSheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String goalDetails = req.getParameter("removeGoal");
        String[] parts = goalDetails.split("-");
        String side = parts[0];
        int goalIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheet(gameId).getBody();

        log.info("Removing {} goal {} from gameSheet {}", side, goalIndex, gameId);
        getTeamGoals(lastUpdatedGamesheet, side).remove(goalIndex);

        assert lastUpdatedGamesheet != null;
        addRostersToModel(model, lastUpdatedGamesheet.getHomeTeam(), lastUpdatedGamesheet.getAwayTeam(), lastUpdatedGamesheet.getSeasonYear());
        gameController.updateGamesheet(lastUpdatedGamesheet);
        model.addAttribute("gameSheet", lastUpdatedGamesheet);
        return "gameSheet";
    }

    private List<GoalPayload> getTeamGoals(GamesheetPayload gameSheet, String side) {
        List<GoalPayload> goals;
        if(side.equals("home")){
            if(CollectionUtils.isEmpty(gameSheet.getHomeGoals())){
                gameSheet.setHomeGoals(new ArrayList<>());
            }
            goals = gameSheet.getHomeGoals();
        } else {
            if(CollectionUtils.isEmpty(gameSheet.getAwayGoals())){
                gameSheet.setAwayGoals(new ArrayList<>());
            }
            goals = gameSheet.getAwayGoals();
        }
        return goals;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"addPenalty"})
    public String addPenalty(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") GamesheetPayload gameSheet,
            final BindingResult bindingResult,
            Model model) {
        log.info("Adding {} penalty to gameSheet {}", gameSheet.getNextPenalty().getTeam(), gameId);
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());

        var errors = getNextPenaltyErrors(gameSheet.getNextPenalty());
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gameSheet";
        }

        if(gameSheet.getNextPenalty().getTeam().equals(gameSheet.getHomeTeam())){
            gameSheet.addHomePenalty(gameSheet.getNextPenalty().getPenalty());
        }
        else{
            gameSheet.addAwayPenalty(gameSheet.getNextPenalty().getPenalty());
        }

        clearNextData(gameSheet, false);

        gameController.updateGamesheet(gameSheet);
        return "gameSheet";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removePenalty"})
    public String removePenalty(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") GamesheetPayload gameSheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String penaltyDetails = req.getParameter("removePenalty");
        String[] parts = penaltyDetails.split("-");
        String side = parts[0];
        int penaltyIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheet(gameId).getBody();

        getTeamPenalties(lastUpdatedGamesheet, side).remove(penaltyIndex);

        assert lastUpdatedGamesheet != null;
        addRostersToModel(model, lastUpdatedGamesheet.getHomeTeam(), lastUpdatedGamesheet.getAwayTeam(), lastUpdatedGamesheet.getSeasonYear());
        gameController.updateGamesheet(lastUpdatedGamesheet);
        model.addAttribute("gameSheet", lastUpdatedGamesheet);
        return "gameSheet";
    }

    private List<PenaltyPayload> getTeamPenalties(GamesheetPayload gameSheet, String side) {
        List<PenaltyPayload> penalties;
        if(side.equals("home")){
            if(CollectionUtils.isEmpty(gameSheet.getHomePenalties())){
                gameSheet.setHomePenalties(new ArrayList<>());
            }
            penalties = gameSheet.getHomePenalties();
        } else {
            if(CollectionUtils.isEmpty(gameSheet.getAwayPenalties())){
                gameSheet.setAwayPenalties(new ArrayList<>());
            }
            penalties = gameSheet.getAwayPenalties();
        }
        return penalties;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value = "/{gameId}", params = {"save"})
    public String saveGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") GamesheetPayload gameSheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Saving gameSheet {}", gameId);
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());
        var errors = getGoalAndPenaltyErrors(gameSheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gameSheet";
        }

        clearNextData(gameSheet, false);
        gameController.updateGamesheet(gameSheet);
        return "gameSheet";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value = "/{gameId}", params = {"finalize"})
    public String finalizeGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") @Valid GamesheetPayload gameSheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Finalizing gameSheet: {}", gameId);
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());
        var errors = getGoalAndPenaltyErrors(gameSheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gameSheet";
        }

        clearNextData(gameSheet, true);
        gameController.postGamesheet(gameSheet, true, false);
        return "gameSheet";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @RequestMapping(value = "/{gameId}", params = {"clear"})
    public String clearGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") @Valid GamesheetPayload gameSheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Resetting gameSheet: {}", gameId);

        GamesheetPayload clearedGamesheet = gameController.clearGamesheet(gameSheet.getFinalized(), gameId).getBody();
        clearNextData(gameSheet, true);
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());
        model.addAttribute("gameSheet", clearedGamesheet);
        return "gameSheet";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @RequestMapping(value = "/{gameId}", params = {"unlock"})
    public String unlockGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gameSheet") @Valid GamesheetPayload gameSheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Unlocking gameSheet: {}", gameId);
        clearNextData(gameSheet, true);

        GamesheetPayload unlockedGamesheet = gameController.unlockGamesheet(gameId).getBody();
        addRostersToModel(model, gameSheet.getHomeTeam(), gameSheet.getAwayTeam(), gameSheet.getSeasonYear());
        model.addAttribute("gameSheet", unlockedGamesheet);
        return "gameSheet";
    }

    private List<ObjectError> getGoalAndPenaltyErrors(GamesheetPayload gameSheet) {
        List<ObjectError> allErrors = new ArrayList<>();
        allErrors.addAll(getGoalPayloadErrors(gameSheet.getHomeGoals(), gameSheet.getHomeAttendanceByPlayerId(), gameSheet.getHomeTeam(), false));
        allErrors.addAll(getGoalPayloadErrors(gameSheet.getAwayGoals(), gameSheet.getAwayAttendanceByPlayerId(), gameSheet.getAwayTeam(), false));
        allErrors.addAll(getPenaltyPayloadErrors(gameSheet.getHomePenalties(), gameSheet.getHomeTeam(), false));
        allErrors.addAll(getPenaltyPayloadErrors(gameSheet.getAwayPenalties(), gameSheet.getAwayTeam(), false));
        return allErrors;
    }
}
