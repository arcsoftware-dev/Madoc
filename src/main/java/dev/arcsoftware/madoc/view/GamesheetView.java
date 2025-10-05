package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.GameController;
import dev.arcsoftware.madoc.controller.RosterController;
import dev.arcsoftware.madoc.enums.GoalType;
import dev.arcsoftware.madoc.model.payload.*;
import dev.arcsoftware.madoc.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/games")
public class GamesheetView {

    private final GameController gameController;
    private final RosterController rosterController;
    private final GameService gameService;

    @Autowired
    public GamesheetView(GameController gameController, RosterController rosterController, GameService gameService) {
        this.gameController = gameController;
        this.rosterController = rosterController;
        this.gameService = gameService;
    }

    @ModelAttribute("goalTypes")
    public List<String> goalTypes() {
        return Arrays.stream(GoalType.values())
                .map(Enum::name)
                .toList();
    }

    private void addRostersToModel(Model model, String homeTeam, String awayTeam, int year) {
        var homeRoster = rosterController.getRosterAssignmentsByTeam(year, homeTeam).getBody();
        var awayRoster = rosterController.getRosterAssignmentsByTeam(year, awayTeam).getBody();
        model.addAttribute("homeTeamPlayers", homeRoster);
        model.addAttribute("awayTeamPlayers", awayRoster);
    }

    @GetMapping("/{gameId}")
    public String getGamesheet(
        @PathVariable("gameId") int gameId,
            Model model
    ) {
        GamesheetPayload gamesheet = gameController.fetchGamesheetPayload(gameId).getBody();

        model.addAttribute("gamesheet", gamesheet);
        assert gamesheet != null;
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());

        return "gamesheet";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"addGoal"})
    public String addGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model) {
        log.info("Adding {} goal to gamesheet {}", gamesheet.getNextGoal().getTeam(), gameId);
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());

        var errors = getNextGoalErrors(gamesheet.getNextGoal(),
                gamesheet.getNextGoal().getTeam().equals(gamesheet.getHomeTeam())
                        ? gamesheet.getHomeAttendanceByPlayerId()
                        : gamesheet.getAwayAttendanceByPlayerId()
        );

        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gamesheet";
        }

        if(gamesheet.getNextGoal().getTeam().equals(gamesheet.getHomeTeam())){
            gamesheet.addHomeGoal(gamesheet.getNextGoal().getGoal());
        }
        else{
            gamesheet.addAwayGoal(gamesheet.getNextGoal().getGoal());
        }

        clearNextData(gamesheet);

        gameController.updateGamesheetPayload(gamesheet);
        return "gamesheet";
    }

    private List<ObjectError> getNextGoalErrors(NextGoalPayload nextGoal, List<AttendancePayload> attendance) {
        List<ObjectError> errors = new ArrayList<>();
        if(nextGoal == null){
            errors.add(new ObjectError("Add Goal", "Next Goal: Data is Null"));
            return errors;
        }
        if(StringUtils.isEmpty(nextGoal.getTeam())){
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
            LocalTime goalTime = null;
            try{
                goalTime = gameService.timeStringToLocalTime(goalPayload.getTime());
            } catch(Exception ignored){}
            if(goalTime == null){
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
        if(StringUtils.isEmpty(nextPenalty.getTeam())){
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
            LocalTime penaltyTime = null;
            try{
                penaltyTime = gameService.timeStringToLocalTime(penaltyPayload.getTime());
            } catch(Exception ignored){}
            if(penaltyTime == null){
                errors.add(new ObjectError(errorMessagePrefix, errorMessagePrefix + "Time Must Be Set in the format 'mm:ss'"));
            }
        }
        return errors;
    }

    private void clearNextData(GamesheetPayload gamesheet) {
        gamesheet.setNextGoal(null);
        gamesheet.setNextPenalty(null);
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removeGoal"})
    public String removeGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String goalDetails = req.getParameter("removeGoal");
        String[] parts = goalDetails.split("-");
        String side = parts[0];
        int goalIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheetPayload(gameId).getBody();

        log.info("Removing {} goal {} from gamesheet {}", side, goalIndex, gameId);
        getTeamGoals(lastUpdatedGamesheet, side).remove(goalIndex);

        addRostersToModel(model, lastUpdatedGamesheet.getHomeTeam(), lastUpdatedGamesheet.getAwayTeam(), lastUpdatedGamesheet.getSeasonYear());
        gameController.updateGamesheetPayload(lastUpdatedGamesheet);
        model.addAttribute("gamesheet", lastUpdatedGamesheet);
        return "gamesheet";
    }

    private List<GoalPayload> getTeamGoals(GamesheetPayload gamesheet, String side) {
        List<GoalPayload> goals;
        if(side.equals("home")){
            if(CollectionUtils.isEmpty(gamesheet.getHomeGoals())){
                gamesheet.setHomeGoals(new ArrayList<>());
            }
            goals = gamesheet.getHomeGoals();
        } else {
            if(CollectionUtils.isEmpty(gamesheet.getAwayGoals())){
                gamesheet.setAwayGoals(new ArrayList<>());
            }
            goals = gamesheet.getAwayGoals();
        }
        return goals;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"addPenalty"})
    public String addPenalty(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model) {
        log.info("Adding {} penalty to gamesheet {}", gamesheet.getNextPenalty().getTeam(), gameId);
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());

        var errors = getNextPenaltyErrors(gamesheet.getNextPenalty());
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gamesheet";
        }

        if(gamesheet.getNextPenalty().getTeam().equals(gamesheet.getHomeTeam())){
            gamesheet.addHomePenalty(gamesheet.getNextPenalty().getPenalty());
        }
        else{
            gamesheet.addAwayPenalty(gamesheet.getNextPenalty().getPenalty());
        }

        clearNextData(gamesheet);

        gameController.updateGamesheetPayload(gamesheet);
        return "gamesheet";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removePenalty"})
    public String removePenalty(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String penaltyDetails = req.getParameter("removePenalty");
        String[] parts = penaltyDetails.split("-");
        String side = parts[0];
        int penaltyIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheetPayload(gameId).getBody();

        getTeamPenalties(lastUpdatedGamesheet, side).remove(penaltyIndex);

        addRostersToModel(model, lastUpdatedGamesheet.getHomeTeam(), lastUpdatedGamesheet.getAwayTeam(), lastUpdatedGamesheet.getSeasonYear());
        gameController.updateGamesheetPayload(lastUpdatedGamesheet);
        model.addAttribute("gamesheet", lastUpdatedGamesheet);
        return "gamesheet";
    }

    private List<PenaltyPayload> getTeamPenalties(GamesheetPayload gamesheet, String side) {
        List<PenaltyPayload> penalties;
        if(side.equals("home")){
            if(CollectionUtils.isEmpty(gamesheet.getHomePenalties())){
                gamesheet.setHomePenalties(new ArrayList<>());
            }
            penalties = gamesheet.getHomePenalties();
        } else {
            if(CollectionUtils.isEmpty(gamesheet.getAwayPenalties())){
                gamesheet.setAwayPenalties(new ArrayList<>());
            }
            penalties = gamesheet.getAwayPenalties();
        }
        return penalties;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value = "/{gameId}", params = {"save"})
    public String saveGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Saving gamesheet {}", gameId);
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());
        var errors = getGoalAndPenaltyErrors(gamesheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gamesheet";
        }

        clearNextData(gamesheet);
        gameController.updateGamesheetPayload(gamesheet);
        return "gamesheet";
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value = "/{gameId}", params = {"finalize"})
    public String finalizeGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") @Valid GamesheetPayload gamesheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Finalizing gamesheet: {}", gameId);
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());
        var errors = getGoalAndPenaltyErrors(gamesheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return "gamesheet";
        }

        clearNextData(gamesheet);
        gameController.postGamesheet(gamesheet, true, false);
        return "gamesheet";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @RequestMapping(value = "/{gameId}", params = {"clear"})
    public String clearGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") @Valid GamesheetPayload gamesheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Resetting gamesheet: {}", gameId);

        GamesheetPayload clearedGamesheet = gameController.clearGamesheet(gamesheet.getFinalized(), gameId).getBody();
        clearNextData(clearedGamesheet);
        addRostersToModel(model, gamesheet.getHomeTeam(), gamesheet.getAwayTeam(), gamesheet.getSeasonYear());
        model.addAttribute("gamesheet", clearedGamesheet);
        return "gamesheet";
    }

    private List<ObjectError> getGoalAndPenaltyErrors(GamesheetPayload gamesheet) {
        List<ObjectError> allErrors = new ArrayList<>();
        allErrors.addAll(getGoalPayloadErrors(gamesheet.getHomeGoals(), gamesheet.getHomeAttendanceByPlayerId(), gamesheet.getHomeTeam(), false));
        allErrors.addAll(getGoalPayloadErrors(gamesheet.getAwayGoals(), gamesheet.getAwayAttendanceByPlayerId(), gamesheet.getAwayTeam(), false));
        allErrors.addAll(getPenaltyPayloadErrors(gamesheet.getHomePenalties(), gamesheet.getHomeTeam(), false));
        allErrors.addAll(getPenaltyPayloadErrors(gamesheet.getAwayPenalties(), gamesheet.getAwayTeam(), false));
        return allErrors;
    }
}
