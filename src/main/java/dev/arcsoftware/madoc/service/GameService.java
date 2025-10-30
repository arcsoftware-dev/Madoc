package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.*;
import dev.arcsoftware.madoc.model.payload.*;
import dev.arcsoftware.madoc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class GameService {
    private final StatsRepository statsRepository;
    private final GameRepository gameRepository;
    private final AttendanceRepository attendanceRepository;
    private final RosterRepository rosterRepository;

    @Autowired
    public GameService(StatsRepository statsRepository,
                       GameRepository gameRepository,
                       AttendanceRepository attendanceRepository,
                       RosterRepository rosterRepository) {
        this.statsRepository = statsRepository;
        this.gameRepository = gameRepository;
        this.attendanceRepository = attendanceRepository;
        this.rosterRepository = rosterRepository;
    }

    private GameEntity validateGamesheetGameEntity(GamesheetPayload gamesheet) {
        GameEntity gameEntity = gameRepository.findById(gamesheet.getGameId()).orElseThrow();
        //Validate everything matches the existing game record
        if(gameEntity.isFinalized() || gamesheet.getFinalized()){
            throw new IllegalStateException("Game already finalized");
        }
        if(gamesheet.getSeasonYear() != gameEntity.getYear()){
            throw new IllegalArgumentException("Season year does not match existing game record");
        }
        if(gamesheet.getSeasonType() != gameEntity.getSeasonType()){
            throw new IllegalArgumentException("Season type does not match existing game record");
        }
        if(!gamesheet.getGameTime().equals(gameEntity.getGameTime())){
            throw new IllegalArgumentException("Game time does not match existing game record");
        }
        if(gamesheet.getVenue() != gameEntity.getVenue()){
            throw new IllegalArgumentException("Venue does not match existing game record");
        }
        if(gamesheet.getHomeTeamId() != gameEntity.getHomeTeam().getId()){
            throw new IllegalArgumentException("Home team does not match existing game record");
        }
        if(gamesheet.getAwayTeamId() != gameEntity.getAwayTeam().getId()){
            throw new IllegalArgumentException("Away team does not match existing game record");
        }
        return gameEntity;
    }

    @Transactional
    public GamesheetSummary createGamesheetSummary(GamesheetPayload gamesheet, boolean submit, boolean createNew) {
        GameEntity game = validateGamesheetGameEntity(gamesheet);

        GamesheetSummary summary = new GamesheetSummary();
        summary.setGameId(gamesheet.getGameId());
        summary.setSeasonType(Objects.requireNonNull(gamesheet.getSeasonType()));
        summary.setSeasonYear(gamesheet.getSeasonYear());
        summary.setGameTime(Objects.requireNonNull(gamesheet.getGameTime()));
        summary.setVenue(Objects.requireNonNull(gamesheet.getVenue()));
        summary.setHomeTeam(gamesheet.getHomeTeam());
        summary.setAwayTeam(gamesheet.getAwayTeam());

        summary.setRefereeOne(gamesheet.getRefereeOne());
        summary.setRefereeTwo(gamesheet.getRefereeTwo());
        summary.setRefereeThree(gamesheet.getRefereeThree());
        summary.setRefereeNotes(gamesheet.getRefereeNotes());

        Optional.ofNullable(gamesheet.getHomeGoals()).orElse(new ArrayList<>())
                .forEach(g -> summary.addHomeGoal(createGoalEntityFromPayload(g, game, gamesheet.getHomeTeamId())));
        Optional.ofNullable(gamesheet.getAwayGoals()).orElse(new ArrayList<>())
                .forEach(g -> summary.addHomeGoal(createGoalEntityFromPayload(g, game, gamesheet.getAwayTeamId())));

        Optional.ofNullable(gamesheet.getHomePenalties()).orElse(new ArrayList<>())
                .forEach(p -> summary.addHomePenalty(createPenaltyEntityFromPayload(p, game, gamesheet.getHomeTeamId())));
        Optional.ofNullable(gamesheet.getAwayPenalties()).orElse(new ArrayList<>())
                .forEach(p -> summary.addAwayPenalty(createPenaltyEntityFromPayload(p, game, gamesheet.getAwayTeamId())));

        summary.setHomeScore(Optional.ofNullable(summary.getHomeGoals()).map(List::size).orElse(0));
        summary.setAwayScore(Optional.ofNullable(summary.getAwayGoals()).map(List::size).orElse(0));

        summary.setHomePenaltyMinutes(
                Optional.ofNullable(summary.getHomePenalties()).orElse(new ArrayList<>())
                    .stream().mapToInt(PenaltyEntity::getMinutes).sum()
        );
        summary.setAwayPenaltyMinutes(
                Optional.ofNullable(summary.getAwayPenalties()).orElse(new ArrayList<>())
                        .stream().mapToInt(PenaltyEntity::getMinutes).sum()
        );

        if(submit){
            log.info("Finalizing and saving all stats for game {}", gamesheet.getGameId());
            //save gamesheet to db
            gamesheet.setFinalized(true);
            updateGamesheetPayload(gamesheet, false, createNew);

            //Update all stats from a gamesheet
            updateStatsFromGamesheet(summary);
            //Update attendance from gamesheet
            insertAttendances(gamesheet.getHomeAttendanceByPlayerId());
            insertAttendances(gamesheet.getAwayAttendanceByPlayerId());
            //finalize the game entity
            finalizeGame(gamesheet, game);
        }
        else {
            log.info("Finished validating Gamesheet for game {}", gamesheet.getGameId());
        }

        return summary;
    }

    private void finalizeGame(GamesheetPayload gamesheet, GameEntity game) {
        game.setRefereeNameOne(gamesheet.getRefereeOne());
        game.setRefereeNameTwo(gamesheet.getRefereeTwo());
        game.setRefereeNameThree(gamesheet.getRefereeThree());
        game.setRefereeNotes(gamesheet.getRefereeNotes());
        game.setFinalized(true);
        game.setFinalizedAt(LocalDateTime.now());
        gameRepository.updateGame(game);
    }

    private void insertAttendances(List<AttendancePayload> attendances) {
        attendances
                .stream()
                .map(this::attendancePayloadToEntity)
                .forEach(attendanceRepository::insertAttendanceEntity);
    }

    private AttendanceEntity attendancePayloadToEntity(AttendancePayload payload){
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setGame(new GameEntity(payload.getGameId()));
        attendanceEntity.setPlayer(new RosterAssignment(payload.getRosterId()));
        attendanceEntity.setJerseyNumber(payload.getJerseyNumber());
        attendanceEntity.setAttended(payload.getAttended());
        return attendanceEntity;
    }

    private void updateStatsFromGamesheet(GamesheetSummary gamesheet) {
        Optional.ofNullable(gamesheet.getHomeGoals()).orElse(new ArrayList<>())
                .forEach(statsRepository::insertGoalAndAssists);

        Optional.ofNullable(gamesheet.getAwayGoals()).orElse(new ArrayList<>())
                .forEach(statsRepository::insertGoalAndAssists);

        Optional.ofNullable(gamesheet.getHomePenalties()).orElse(new ArrayList<>())
                .forEach(statsRepository::insertPenalty);

        Optional.ofNullable(gamesheet.getAwayPenalties()).orElse(new ArrayList<>())
                .forEach(statsRepository::insertPenalty);
    }

    private PenaltyEntity createPenaltyEntityFromPayload(PenaltyPayload penalty, GameEntity game, int teamId) {
        PenaltyEntity penaltyEntity = new PenaltyEntity();

        int offenderId = rosterRepository.findRosterIdByJerseyNumberAndTeam(penalty.getJerseyNumber(), teamId);
        penaltyEntity.setGame(game);
        penaltyEntity.setPlayer(new RosterAssignment(offenderId));
        penaltyEntity.setInfraction(Objects.requireNonNull(penalty.getInfraction()));
        penaltyEntity.setMinutes(Objects.requireNonNull(penalty.getMinutes()));
        penaltyEntity.setPeriod(Objects.requireNonNull(penalty.getPeriod()));
        penaltyEntity.setTime(timeStringToLocalTime(penalty.getTime()));

        penaltyEntity.setJerseyNumber(Objects.requireNonNull(penalty.getJerseyNumber()));

        return penaltyEntity;
    }

    private GoalEntity createGoalEntityFromPayload(GoalPayload goal, GameEntity game, int teamId) {
        GoalEntity goalEntity = new GoalEntity();

        goalEntity.setGame(game);
        goalEntity.setGoalType(goal.getGoalType());
        //Set the roster id for goal, primary and secondary assists from players table
        int scorerId = rosterRepository.findRosterIdByJerseyNumberAndTeam(goal.getScorer(), teamId);
        goalEntity.setScorer(new RosterAssignment(scorerId));

        if(goal.getAssist1() != null){
            int primaryAssistRosterId = rosterRepository.findRosterIdByJerseyNumberAndTeam(goal.getAssist1(), teamId);
            goalEntity.setPrimaryAssistPlayer(new RosterAssignment(primaryAssistRosterId));
        }
        if(goal.getAssist2() != null){
            int secondaryAssistRosterId = rosterRepository.findRosterIdByJerseyNumberAndTeam(goal.getAssist2(), teamId);
            goalEntity.setSecondaryAssistPlayer(new RosterAssignment(secondaryAssistRosterId));
        }
        goalEntity.setPeriod(goal.getPeriod());
        goalEntity.setTime(timeStringToLocalTime(goal.getTime()));

        goalEntity.setJerseyNumber(goal.getScorer());
        goalEntity.setPrimaryAssistJerseyNumber(goal.getAssist1());
        goalEntity.setSecondaryAssistJerseyNumber(goal.getAssist2());

        return goalEntity;
    }

    @Transactional
    public GamesheetPayload updateGamesheetPayload(GamesheetPayload gamesheet, boolean validate, boolean createNew) {
        if(validate){
            validateGamesheetGameEntity(gamesheet);
        }

        //sort Goals and penalties by period then time
        sortStats(gamesheet);

        if(createNew){
            gameRepository.insertNewGamesheetPayload(gamesheet);
        }
        else{
            gameRepository.updateGamesheetPayload(gamesheet);
        }

        return gamesheet;
    }

    private void sortStats(GamesheetPayload gamesheet) {
        sortGoals(gamesheet.getHomeGoals());
        sortGoals(gamesheet.getAwayGoals());
        sortPenalties(gamesheet.getHomePenalties());
        sortPenalties(gamesheet.getAwayPenalties());
    }

    private void sortGoals(List<GoalPayload> goals){
        Optional.ofNullable(goals).orElse(new ArrayList<>())
                .sort(
                        Comparator.comparing(GoalPayload::getPeriod)
                                .thenComparing(g -> timeStringToLocalTime(g.getTime()), Comparator.reverseOrder())
                );
    }

    private void sortPenalties(List<PenaltyPayload> penalties){
        Optional.ofNullable(penalties).orElse(new ArrayList<>())
                .sort(
                        Comparator.comparing(PenaltyPayload::getPeriod)
                                .thenComparing(p -> timeStringToLocalTime(p.getTime()), Comparator.reverseOrder())
                );
    }

    public GamesheetPayload fetchGamesheetPayloadByGameId(int gameId) {
        Optional<GamesheetPayload> gamesheetOpt = gameRepository.fetchGamesheetPayloadByGameId(gameId);
        GamesheetPayload gamesheet;
        if(gamesheetOpt.isEmpty()){
            log.info("No gamesheet found for game {}.  Creating new gamesheet", gameId);
            gamesheet = createNewGamesheet(gameId);
        }
        else{
            gamesheet = gamesheetOpt.get();
        }

        return gamesheet;
    }

    private GamesheetPayload createNewGamesheet(int gameId) {
        GameEntity gameEntity = gameRepository.findById(gameId).orElseThrow();
        GamesheetPayload gamesheet = new GamesheetPayload();
        gamesheet.setGameId(gameId);
        gamesheet.setFinalized(gameEntity.isFinalized());
        gamesheet.setGameTime(gameEntity.getGameTime());
        gamesheet.setVenue(gameEntity.getVenue());
        gamesheet.setSeasonYear(gameEntity.getYear());
        gamesheet.setSeasonType(gameEntity.getSeasonType());
        gamesheet.setHomeTeam(gameEntity.getHomeTeam().getTeamName());
        gamesheet.setHomeTeamId(gameEntity.getHomeTeam().getId());
        gamesheet.setAwayTeam(gameEntity.getAwayTeam().getTeamName());
        gamesheet.setAwayTeamId(gameEntity.getAwayTeam().getId());

        gameRepository.insertNewGamesheetPayload(gamesheet);

        return gamesheet;
    }

    public LocalTime timeStringToLocalTime(String timeString) {
        if(timeString == null || timeString.isBlank()){
            throw new IllegalArgumentException("Invalid time string: " + timeString);
        }
        String[] parts = timeString.split(":");
        if(parts.length == 1){
            //only seconds provided
            String seconds = StringUtils.leftPad(parts[0], 2, '0');
            return LocalTime.parse("00:00:"+timeString);
        }
        else if(parts.length == 2){
            //seconds and minutes provided
            String minutes = StringUtils.leftPad(parts[0], 2, '0');
            String seconds = StringUtils.leftPad(parts[1], 2, '0');
            return LocalTime.parse("00:"+minutes+":"+seconds);
        }
        else{
            throw new IllegalArgumentException("Invalid time string: " + timeString);
        }
    }

    @Transactional
    public GamesheetPayload clearGamesheet(int gameId, boolean gameIsFinalized) {
        GamesheetPayload gamesheet = gameRepository.fetchGamesheetPayloadByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("No gamesheet found for game id " + gameId));
        if(gameIsFinalized != gamesheet.getFinalized()){
            throw new IllegalArgumentException("Gamesheet finalized state doesnt match request");
        }

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game id " + gameId + " does not exist"));
        if(gameIsFinalized != gameEntity.isFinalized()){
            throw new IllegalArgumentException("Game Entity finalized state doesnt match request");

        }

        //Clear Gamesheet
        gamesheet.setHomeGoals(null);
        gamesheet.setAwayGoals(null);
        gamesheet.setHomePenalties(null);
        gamesheet.setAwayPenalties(null);
        Optional.ofNullable(gamesheet.getHomeAttendanceByPlayerId()).orElse(new ArrayList<>()).forEach(a -> a.setAttended(false));
        Optional.ofNullable(gamesheet.getAwayAttendanceByPlayerId()).orElse(new ArrayList<>()).forEach(a -> a.setAttended(false));
        gamesheet.setFinalized(false);
        gameRepository.updateGamesheetPayload(gamesheet);

        //Clear game
        gameEntity.setFinalizedAt(null);
        gameEntity.setFinalized(false);
        gameEntity.setRefereeNameOne(null);
        gameEntity.setRefereeNameTwo(null);
        gameEntity.setRefereeNameThree(null);
        gameEntity.setRefereeNotes(new ArrayList<>());
        gameRepository.updateGame(gameEntity);

        //Clear goals, assists and penalty tables
        statsRepository.clearStatsByGameId(gameId);
        //Clear attendance table
        attendanceRepository.clearByGameId(gameId);

        return gamesheet;
    }

    @Transactional
    public GamesheetPayload unlockGamesheet(int gameId) {
        GamesheetPayload gamesheet = gameRepository.fetchGamesheetPayloadByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("No gamesheet found for game id " + gameId));

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game id " + gameId + " does not exist"));

        //Clear Gamesheet
        gamesheet.setFinalized(false);
        gameRepository.updateGamesheetPayload(gamesheet);

        //Clear game
        gameEntity.setFinalizedAt(null);
        gameEntity.setFinalized(false);
        gameRepository.updateGame(gameEntity);

        //Clear goals, assists and penalty tables
        statsRepository.clearStatsByGameId(gameId);
        //Clear attendance table
        attendanceRepository.clearByGameId(gameId);
        return gamesheet;
    }
}
