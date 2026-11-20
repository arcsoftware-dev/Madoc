package dev.arcsoftware.madoc.view;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.arcsoftware.madoc.controller.GameController;
import dev.arcsoftware.madoc.controller.RosterController;
import dev.arcsoftware.madoc.enums.GoalType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.payload.AttendancePayload;
import dev.arcsoftware.madoc.model.payload.GamesheetPayload;
import dev.arcsoftware.madoc.model.payload.GoalPayload;
import dev.arcsoftware.madoc.model.payload.NextGoalPayload;
import dev.arcsoftware.madoc.model.payload.NextPenaltyPayload;
import dev.arcsoftware.madoc.model.payload.PenaltyPayload;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.service.GameService;
import dev.arcsoftware.madoc.service.ScheduleService;
import dev.arcsoftware.madoc.service.StandingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/games")
public class GamesheetView {

    private static final String GAMESHEET_VIEW = "gamesheet-modern";

    private final GameController gameController;
    private final RosterController rosterController;
    private final GameService gameService;
    private final ScheduleService scheduleService;
    private final StandingsService standingsService;

    @Autowired
    public GamesheetView(GameController gameController,
                         RosterController rosterController,
                         GameService gameService,
                         ScheduleService scheduleService,
                         StandingsService standingsService) {
        this.gameController = gameController;
        this.rosterController = rosterController;
        this.gameService = gameService;
        this.scheduleService = scheduleService;
        this.standingsService = standingsService;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchupGame {
        private final int gameId;
        private final LocalDateTime startTime;
        private final String seasonLabel;
        private final boolean playoffs;
        private final String homeTeam;
        private final String awayTeam;
        private final int homeScore;
        private final int awayScore;
        private final String winnerTeam;
        private final boolean tie;
    }

    @Getter
    @AllArgsConstructor
    public static class BoxscoreGoal {
        private final int period;
        private final String time;
        private final String teamName;
        private final boolean home;
        private final String scorer;
        private final String assists;
        private final String goalTypeBadge;
    }

    @Getter
    @AllArgsConstructor
    public static class BoxscorePenalty {
        private final int period;
        private final String time;
        private final String teamName;
        private final boolean home;
        private final String player;
        private final String infraction;
        private final int minutes;
    }

    @ModelAttribute("goalTypes")
    public List<String> goalTypes() {
        return Arrays.stream(GoalType.values())
                .map(Enum::name)
                .toList();
    }

    private void populateModel(Model model, GamesheetPayload gamesheet) {
        int year = gamesheet.getSeasonYear();
        String homeTeam = gamesheet.getHomeTeam();
        String awayTeam = gamesheet.getAwayTeam();
        SeasonType seasonType = gamesheet.getSeasonType() != null ? gamesheet.getSeasonType() : SeasonType.REGULAR_SEASON;

        List<RosterAssignmentDto> allHomeRoster = Optional.ofNullable(
                rosterController.getRosterAssignmentsByTeam(year, homeTeam).getBody()).orElse(List.of());
        List<RosterAssignmentDto> allAwayRoster = Optional.ofNullable(
                rosterController.getRosterAssignmentsByTeam(year, awayTeam).getBody()).orElse(List.of());

        List<RosterAssignmentDto> homeRoster = allHomeRoster.stream().filter(RosterAssignment::isActive).toList();
        List<RosterAssignmentDto> awayRoster = allAwayRoster.stream().filter(RosterAssignment::isActive).toList();
        model.addAttribute("homeTeamPlayers", homeRoster);
        model.addAttribute("awayTeamPlayers", awayRoster);

        Map<Integer, String> homePlayerNames = buildPlayerNameMap(allHomeRoster, gamesheet.getHomeAttendanceByPlayerId());
        Map<Integer, String> awayPlayerNames = buildPlayerNameMap(allAwayRoster, gamesheet.getAwayAttendanceByPlayerId());

        addStandingsToModel(model, homeTeam, awayTeam, year, seasonType);
        addSeasonHistoryToModel(model, gamesheet, year, seasonType);
        addBoxscoreToModel(model, gamesheet, homePlayerNames, awayPlayerNames);
    }

    private Map<Integer, String> buildPlayerNameMap(List<RosterAssignmentDto> roster, List<AttendancePayload> attendance) {
        Map<Integer, String> names = new HashMap<>();
        for (RosterAssignmentDto player : roster) {
            if (player.getJerseyNumber() != null && player.getFullName() != null) {
                names.putIfAbsent(player.getJerseyNumber(), player.getFullName());
            }
        }
        if (attendance != null) {
            for (AttendancePayload att : attendance) {
                if (att.getJerseyNumber() != null && att.getPlayerName() != null) {
                    names.putIfAbsent(att.getJerseyNumber(), att.getPlayerName());
                }
            }
        }
        return names;
    }

    private void addStandingsToModel(Model model, String homeTeam, String awayTeam, int year, SeasonType seasonType) {
        SeasonType effectiveSeasonType = seasonType;
        List<TeamStatsDto> standings = standingsService.getStandings(
                new StandingsRequest(year, effectiveSeasonType, StandingsCategory.POINTS, SortOrder.DESC));

        TeamStatsDto homeStats = findTeamStats(standings, homeTeam);
        TeamStatsDto awayStats = findTeamStats(standings, awayTeam);

        if (homeStats == null && awayStats == null && seasonType == SeasonType.PLAYOFFS) {
            effectiveSeasonType = SeasonType.REGULAR_SEASON;
            standings = standingsService.getStandings(
                    new StandingsRequest(year, effectiveSeasonType, StandingsCategory.POINTS, SortOrder.DESC));
            homeStats = findTeamStats(standings, homeTeam);
            awayStats = findTeamStats(standings, awayTeam);
        }

        model.addAttribute("homeTeamStats", homeStats);
        model.addAttribute("awayTeamStats", awayStats);
        model.addAttribute("homeTeamRank", findTeamRank(standings, homeTeam));
        model.addAttribute("awayTeamRank", findTeamRank(standings, awayTeam));
        model.addAttribute("statsSeasonLabel", effectiveSeasonType.getLabel());
    }

    private TeamStatsDto findTeamStats(List<TeamStatsDto> standings, String teamName) {
        if (standings == null || teamName == null) {
            return null;
        }
        return standings.stream()
                .filter(s -> teamName.equalsIgnoreCase(s.getTeamName()))
                .findFirst()
                .orElse(null);
    }

    private Integer findTeamRank(List<TeamStatsDto> standings, String teamName) {
        if (standings == null || teamName == null) {
            return null;
        }
        for (int i = 0; i < standings.size(); i++) {
            if (teamName.equalsIgnoreCase(standings.get(i).getTeamName())) {
                return i + 1;
            }
        }
        return null;
    }

    private void addSeasonHistoryToModel(Model model, GamesheetPayload gamesheet, int year, SeasonType seasonType) {
        String homeTeam = gamesheet.getHomeTeam();
        String awayTeam = gamesheet.getAwayTeam();
        int currentGameId = gamesheet.getGameId();

        List<MatchupGame> previousMatchups = new ArrayList<>();
        collectMatchups(previousMatchups, scheduleService.getSchedule(SeasonType.REGULAR_SEASON, year),
                SeasonType.REGULAR_SEASON, homeTeam, awayTeam, currentGameId);
        if (seasonType == SeasonType.PLAYOFFS) {
            collectMatchups(previousMatchups, scheduleService.getSchedule(SeasonType.PLAYOFFS, year),
                    SeasonType.PLAYOFFS, homeTeam, awayTeam, currentGameId);
        }

        previousMatchups.sort(Comparator.comparing(MatchupGame::getStartTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int seriesHomeWins = 0;
        int seriesAwayWins = 0;
        int seriesTies = 0;
        int seriesHomeGoals = 0;
        int seriesAwayGoals = 0;

        for (MatchupGame game : previousMatchups) {
            boolean sameOrientation = homeTeam.equalsIgnoreCase(game.getHomeTeam());
            int hGoals = sameOrientation ? game.getHomeScore() : game.getAwayScore();
            int aGoals = sameOrientation ? game.getAwayScore() : game.getHomeScore();
            seriesHomeGoals += hGoals;
            seriesAwayGoals += aGoals;

            if (game.isTie()) {
                seriesTies++;
            } else if (homeTeam.equalsIgnoreCase(game.getWinnerTeam())) {
                seriesHomeWins++;
            } else {
                seriesAwayWins++;
            }
        }

        model.addAttribute("previousMatchups", previousMatchups);
        model.addAttribute("seriesHomeWins", seriesHomeWins);
        model.addAttribute("seriesAwayWins", seriesAwayWins);
        model.addAttribute("seriesTies", seriesTies);
        model.addAttribute("seriesHomeGoals", seriesHomeGoals);
        model.addAttribute("seriesAwayGoals", seriesAwayGoals);
    }

    private void collectMatchups(List<MatchupGame> target,
                                 List<ScheduleItemDto> schedule,
                                 SeasonType type,
                                 String homeTeam,
                                 String awayTeam,
                                 int currentGameId) {
        if (schedule == null) {
            return;
        }
        for (ScheduleItemDto item : schedule) {
            if (item.getGameId() == null || item.getGameId() == currentGameId) {
                continue;
            }
            if (item.getHomeScore() == null || item.getAwayScore() == null) {
                continue;
            }
            boolean matchesPair = (homeTeam.equalsIgnoreCase(item.getHomeTeam()) && awayTeam.equalsIgnoreCase(item.getAwayTeam()))
                    || (homeTeam.equalsIgnoreCase(item.getAwayTeam()) && awayTeam.equalsIgnoreCase(item.getHomeTeam()));
            if (!matchesPair) {
                continue;
            }

            try {
                int homeScore = Integer.parseInt(item.getHomeScore().trim());
                int awayScore = Integer.parseInt(item.getAwayScore().trim());
                boolean tie = homeScore == awayScore;
                String winner = tie ? null : (homeScore > awayScore ? item.getHomeTeam() : item.getAwayTeam());
                target.add(new MatchupGame(
                        item.getGameId(),
                        item.getStartTime(),
                        type.getLabel(),
                        type == SeasonType.PLAYOFFS,
                        item.getHomeTeam(),
                        item.getAwayTeam(),
                        homeScore,
                        awayScore,
                        winner,
                        tie
                ));
            } catch (NumberFormatException ignored) {
                // Skip games with unparseable scores
            }
        }
    }

    private void addBoxscoreToModel(Model model,
                                    GamesheetPayload gamesheet,
                                    Map<Integer, String> homePlayerNames,
                                    Map<Integer, String> awayPlayerNames) {
        int[] homePeriodGoals = new int[4];
        int[] awayPeriodGoals = new int[4];
        List<BoxscoreGoal> goals = new ArrayList<>();

        if (gamesheet.getHomeGoals() != null) {
            for (GoalPayload g : gamesheet.getHomeGoals()) {
                int p = g.getPeriod() != null ? g.getPeriod() : 0;
                if (p >= 1 && p <= 4) {
                    homePeriodGoals[p - 1]++;
                }
                goals.add(toBoxscoreGoal(g, gamesheet.getHomeTeam(), true, homePlayerNames));
            }
        }
        if (gamesheet.getAwayGoals() != null) {
            for (GoalPayload g : gamesheet.getAwayGoals()) {
                int p = g.getPeriod() != null ? g.getPeriod() : 0;
                if (p >= 1 && p <= 4) {
                    awayPeriodGoals[p - 1]++;
                }
                goals.add(toBoxscoreGoal(g, gamesheet.getAwayTeam(), false, awayPlayerNames));
            }
        }
        goals.sort(Comparator.comparingInt(BoxscoreGoal::getPeriod)
                .thenComparing(g -> safeParseTime(g.getTime()), Comparator.reverseOrder()));

        List<BoxscorePenalty> penalties = new ArrayList<>();
        if (gamesheet.getHomePenalties() != null) {
            for (PenaltyPayload p : gamesheet.getHomePenalties()) {
                penalties.add(toBoxscorePenalty(p, gamesheet.getHomeTeam(), true, homePlayerNames));
            }
        }
        if (gamesheet.getAwayPenalties() != null) {
            for (PenaltyPayload p : gamesheet.getAwayPenalties()) {
                penalties.add(toBoxscorePenalty(p, gamesheet.getAwayTeam(), false, awayPlayerNames));
            }
        }
        penalties.sort(Comparator.comparingInt(BoxscorePenalty::getPeriod)
                .thenComparing(p -> safeParseTime(p.getTime()), Comparator.reverseOrder()));

        model.addAttribute("homePeriodGoals", homePeriodGoals);
        model.addAttribute("awayPeriodGoals", awayPeriodGoals);
        model.addAttribute("hasOvertime", homePeriodGoals[3] > 0 || awayPeriodGoals[3] > 0);
        model.addAttribute("boxscoreGoals", goals);
        model.addAttribute("boxscorePenalties", penalties);
    }

    private BoxscoreGoal toBoxscoreGoal(GoalPayload g, String teamName, boolean isHome, Map<Integer, String> playerNames) {
        String scorer = formatPlayer(g.getScorer(), playerNames);
        List<String> assistList = new ArrayList<>();
        if (g.getAssist1() != null) {
            assistList.add(formatPlayer(g.getAssist1(), playerNames));
        }
        if (g.getAssist2() != null) {
            assistList.add(formatPlayer(g.getAssist2(), playerNames));
        }
        String assists = assistList.isEmpty() ? "Unassisted" : String.join(", ", assistList);
        return new BoxscoreGoal(
                g.getPeriod() != null ? g.getPeriod() : 0,
                g.getTime() != null ? g.getTime() : "",
                teamName,
                isHome,
                scorer,
                assists,
                goalTypeBadge(g.getGoalType())
        );
    }

    private BoxscorePenalty toBoxscorePenalty(PenaltyPayload p, String teamName, boolean isHome, Map<Integer, String> playerNames) {
        return new BoxscorePenalty(
                p.getPeriod() != null ? p.getPeriod() : 0,
                p.getTime() != null ? p.getTime() : "",
                teamName,
                isHome,
                formatPlayer(p.getJerseyNumber(), playerNames),
                p.getInfraction() != null ? p.getInfraction() : "",
                p.getMinutes() != null ? p.getMinutes() : 0
        );
    }

    private String formatPlayer(Integer jerseyNumber, Map<Integer, String> playerNames) {
        if (jerseyNumber == null) {
            return "—";
        }
        String name = playerNames.get(jerseyNumber);
        return name != null ? "#" + jerseyNumber + " " + name : "#" + jerseyNumber;
    }

    private String goalTypeBadge(GoalType type) {
        if (type == null || type == GoalType.REGULAR) {
            return "";
        }
        return switch (type) {
            case POWER_PLAY -> "PP";
            case SHORT_HANDED -> "SH";
            case EMPTY_NET -> "EN";
            case PENALTY_SHOT -> "PS";
            default -> "";
        };
    }

    private LocalTime safeParseTime(String time) {
        if (time == null || time.isBlank()) {
            return LocalTime.MIN;
        }
        try {
            return gameService.timeStringToLocalTime(time);
        } catch (Exception e) {
            return LocalTime.MIN;
        }
    }

    @GetMapping("/{gameId}")
    public String getGamesheet(
            @PathVariable("gameId") int gameId,
            Model model
    ) {
        GamesheetPayload gamesheet = gameController.fetchGamesheet(gameId).getBody();

        model.addAttribute("gamesheet", gamesheet);
        assert gamesheet != null;
        populateModel(model, gamesheet);
        if (!gamesheet.getFinalized()) {
            applyDefaultNextTeamsFromModel(gamesheet, model);
        }

        return GAMESHEET_VIEW;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"addGoal"})
    public String addGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model) {
        String lastGoalTeam = getSelectedGoalTeam(gamesheet);
        String lastPenaltyTeam = getSelectedPenaltyTeam(gamesheet);
        log.info("Adding {} goal to gamesheet {}", lastGoalTeam, gameId);
        populateModel(model, gamesheet);

        var errors = getNextGoalErrors(gamesheet.getNextGoal(),
                gamesheet.getNextGoal().getTeam().equals(gamesheet.getHomeTeam())
                        ? gamesheet.getHomeAttendanceByPlayerId()
                        : gamesheet.getAwayAttendanceByPlayerId()
        );

        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return GAMESHEET_VIEW;
        }

        if(gamesheet.getNextGoal().getTeam().equals(gamesheet.getHomeTeam())){
            gamesheet.addHomeGoal(gamesheet.getNextGoal().getGoal());
        }
        else{
            gamesheet.addAwayGoal(gamesheet.getNextGoal().getGoal());
        }

        clearNextData(gamesheet);

        gameController.updateGamesheet(gamesheet);
        applyDefaultNextTeams(gamesheet, lastGoalTeam, lastPenaltyTeam);
        populateModel(model, gamesheet);
        return GAMESHEET_VIEW;
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

    private void clearNextData(GamesheetPayload gamesheet) {
        gamesheet.setNextGoal(null);
        gamesheet.setNextPenalty(null);
    }

    private String getSelectedGoalTeam(GamesheetPayload gamesheet) {
        return gamesheet != null && gamesheet.getNextGoal() != null
                ? gamesheet.getNextGoal().getTeam()
                : null;
    }

    private String getSelectedPenaltyTeam(GamesheetPayload gamesheet) {
        return gamesheet != null && gamesheet.getNextPenalty() != null
                ? gamesheet.getNextPenalty().getTeam()
                : null;
    }

    private void applyDefaultNextTeams(GamesheetPayload gamesheet, String goalTeam, String penaltyTeam) {
        if (gamesheet == null) {
            return;
        }
        if (StringUtils.hasText(goalTeam)) {
            NextGoalPayload nextGoal = new NextGoalPayload();
            nextGoal.setTeam(goalTeam);
            nextGoal.setGoal(new GoalPayload());
            gamesheet.setNextGoal(nextGoal);
        }
        if (StringUtils.hasText(penaltyTeam)) {
            NextPenaltyPayload nextPenalty = new NextPenaltyPayload();
            nextPenalty.setTeam(penaltyTeam);
            nextPenalty.setPenalty(new PenaltyPayload());
            gamesheet.setNextPenalty(nextPenalty);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyDefaultNextTeamsFromModel(GamesheetPayload gamesheet, Model model) {
        String lastGoalTeam = null;
        Object goalsAttr = model.asMap().get("boxscoreGoals");
        if (goalsAttr instanceof List<?> goalList && !goalList.isEmpty()) {
            Object last = goalList.get(goalList.size() - 1);
            if (last instanceof BoxscoreGoal bg) {
                lastGoalTeam = bg.getTeamName();
            }
        }

        String lastPenaltyTeam = null;
        Object penaltiesAttr = model.asMap().get("boxscorePenalties");
        if (penaltiesAttr instanceof List<?> penaltyList && !penaltyList.isEmpty()) {
            Object last = penaltyList.get(penaltyList.size() - 1);
            if (last instanceof BoxscorePenalty bp) {
                lastPenaltyTeam = bp.getTeamName();
            }
        }

        applyDefaultNextTeams(gamesheet, lastGoalTeam, lastPenaltyTeam);
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removeGoal"})
    public String removeGoal(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String lastGoalTeam = getSelectedGoalTeam(gamesheet);
        String lastPenaltyTeam = getSelectedPenaltyTeam(gamesheet);
        String goalDetails = req.getParameter("removeGoal");
        String[] parts = goalDetails.split("-");
        String side = parts[0];
        int goalIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheet(gameId).getBody();

        log.info("Removing {} goal {} from gamesheet {}", side, goalIndex, gameId);
        getTeamGoals(lastUpdatedGamesheet, side).remove(goalIndex);

        gameController.updateGamesheet(lastUpdatedGamesheet);
        applyDefaultNextTeams(lastUpdatedGamesheet, lastGoalTeam, lastPenaltyTeam);
        populateModel(model, lastUpdatedGamesheet);
        model.addAttribute("gamesheet", lastUpdatedGamesheet);
        return GAMESHEET_VIEW;
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
        String lastGoalTeam = getSelectedGoalTeam(gamesheet);
        String lastPenaltyTeam = getSelectedPenaltyTeam(gamesheet);
        log.info("Adding {} penalty to gamesheet {}", lastPenaltyTeam, gameId);
        populateModel(model, gamesheet);

        var errors = getNextPenaltyErrors(gamesheet.getNextPenalty());
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return GAMESHEET_VIEW;
        }

        if(gamesheet.getNextPenalty().getTeam().equals(gamesheet.getHomeTeam())){
            gamesheet.addHomePenalty(gamesheet.getNextPenalty().getPenalty());
        }
        else{
            gamesheet.addAwayPenalty(gamesheet.getNextPenalty().getPenalty());
        }

        clearNextData(gamesheet);

        gameController.updateGamesheet(gamesheet);
        applyDefaultNextTeams(gamesheet, lastGoalTeam, lastPenaltyTeam);
        populateModel(model, gamesheet);
        return GAMESHEET_VIEW;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @RequestMapping(value="/{gameId}", params={"removePenalty"})
    public String removePenalty(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") GamesheetPayload gamesheet,
            final BindingResult bindingResult,
            Model model,
            final HttpServletRequest req) {
        String lastGoalTeam = getSelectedGoalTeam(gamesheet);
        String lastPenaltyTeam = getSelectedPenaltyTeam(gamesheet);
        String penaltyDetails = req.getParameter("removePenalty");
        String[] parts = penaltyDetails.split("-");
        String side = parts[0];
        int penaltyIndex = Integer.parseInt(parts[1]);

        GamesheetPayload lastUpdatedGamesheet = gameController.fetchGamesheet(gameId).getBody();

        getTeamPenalties(lastUpdatedGamesheet, side).remove(penaltyIndex);

        gameController.updateGamesheet(lastUpdatedGamesheet);
        applyDefaultNextTeams(lastUpdatedGamesheet, lastGoalTeam, lastPenaltyTeam);
        populateModel(model, lastUpdatedGamesheet);
        model.addAttribute("gamesheet", lastUpdatedGamesheet);
        return GAMESHEET_VIEW;
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
        String lastGoalTeam = getSelectedGoalTeam(gamesheet);
        String lastPenaltyTeam = getSelectedPenaltyTeam(gamesheet);
        log.info("Saving gamesheet {}", gameId);
        populateModel(model, gamesheet);
        var errors = getGoalAndPenaltyErrors(gamesheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return GAMESHEET_VIEW;
        }

        clearNextData(gamesheet);
        gameController.updateGamesheet(gamesheet);
        applyDefaultNextTeams(gamesheet, lastGoalTeam, lastPenaltyTeam);
        populateModel(model, gamesheet);
        return GAMESHEET_VIEW;
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
        populateModel(model, gamesheet);
        var errors = getGoalAndPenaltyErrors(gamesheet);
        if(!errors.isEmpty()){
            for(ObjectError error : errors){
                bindingResult.addError(error);
            }
            return GAMESHEET_VIEW;
        }

        clearNextData(gamesheet);
        gameController.postGamesheet(gamesheet, true, false);
        populateModel(model, gamesheet);
        return GAMESHEET_VIEW;
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
        populateModel(model, clearedGamesheet);
        model.addAttribute("gamesheet", clearedGamesheet);
        return GAMESHEET_VIEW;
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @RequestMapping(value = "/{gameId}", params = {"unlock"})
    public String unlockGamesheet(
            @PathVariable("gameId") int gameId,
            @ModelAttribute("gamesheet") @Valid GamesheetPayload gamesheet,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Unlocking gamesheet: {}", gameId);
        clearNextData(gamesheet);

        GamesheetPayload unlockedGamesheet = gameController.unlockGamesheet(gameId).getBody();
        populateModel(model, unlockedGamesheet);
        applyDefaultNextTeamsFromModel(unlockedGamesheet, model);
        model.addAttribute("gamesheet", unlockedGamesheet);
        return GAMESHEET_VIEW;
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
