package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.*;
import dev.arcsoftware.madoc.model.payload.*;
import dev.arcsoftware.madoc.repository.*;
import dev.arcsoftware.madoc.util.FileUploadParser;
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
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    private final FileUploadParser fileUploadParser;
    private final RosterRepository rosterRepository;

    @Autowired
    public GameService(StatsRepository statsRepository,
                       GameRepository gameRepository,
                       AttendanceRepository attendanceRepository,
                       PlayerRepository playerRepository,
                       TeamRepository teamRepository,
                       FileUploadParser fileUploadParser,
                       RosterRepository rosterRepository) {
        this.statsRepository = statsRepository;
        this.gameRepository = gameRepository;
        this.attendanceRepository = attendanceRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.fileUploadParser = fileUploadParser;
        this.rosterRepository = rosterRepository;
    }

    @Transactional
    public GamesheetSummary uploadGamesheet(GameUploadData uploadFileData) {
        GamesheetSummary summary = fileUploadParser.parseGamesheet(uploadFileData.getFileContent());
        uploadFileData.setYear(summary.getSeasonYear());

        //Handle insertion of goals/penalties and updating of game entity
        int gameId = processGamesheetSummmary(summary);

        //Insert uploadFileData
        uploadFileData.setGameId(gameId);
        uploadFileData.setFileName(
                summary.getHomeTeam()
                + "-vs-"
                + summary.getAwayTeam()
                + "-"
                + summary.getGameTime().toString()
                + ".csv"
        );
        gameRepository.insertGamesheetUpload(uploadFileData);

        summary.setGamesheetFileId(uploadFileData.getId());
        summary.setGamesheetFileName(uploadFileData.getFileName());

        return summary;
    }


    @Transactional
    public AttendanceUploadResult uploadAttendance(GameUploadData uploadFileData) {
        AttendanceUploadResult result = fileUploadParser.parseAttendanceSheet(uploadFileData.getFileContent());

        //Handle insertion of attendance entities
        int gameId = processAttendanceEntities(result);

        //Insert uploadFileData
        uploadFileData.setGameId(gameId);
        uploadFileData.setFileName(
                result.getTeamName()
                        + "-game"
                        + gameId
                        + "-attendance"
                        + ".csv"
        );
        attendanceRepository.insertAttendanceUpload(uploadFileData);

        result.setAttendanceFileId(uploadFileData.getId());
        result.setAttendanceFileName(uploadFileData.getFileName());

        return result;
    }

    private int processAttendanceEntities(AttendanceUploadResult result) {
        //Find game entity by date/seasonYear/seasonType
        GameEntity foundGame = gameRepository.findByDateYearAndSeasonType(
                result.getGameTime(),
                result.getSeasonYear(),
                result.getSeasonType()
        );
        int gameId = foundGame.getId();

        int teamId = teamRepository.findTeamIdByNameAndYear(result.getTeamName(), result.getSeasonYear());

        //Insert Entities
        for(AttendanceUploadResult.AttendanceDetail homeAttendanceValue : result.getAttendanceDetails()){
            AttendanceEntity entity = new AttendanceEntity();
            entity.setGame(foundGame);
            entity.setJerseyNumber(homeAttendanceValue.getJerseyNumber());
            entity.setAttended(homeAttendanceValue.isAttended());
            populateAttendanceEntity(entity, teamId);
            attendanceRepository.insertAttendanceEntity(entity);
        }

        return gameId;
    }

    private int processGamesheetSummmary(GamesheetSummary summary) {
        //Find game entity by date/seasonYear/seasonType
        GameEntity foundGame = gameRepository.findByDateYearAndSeasonType(
                summary.getGameTime(),
                summary.getSeasonYear(),
                summary.getSeasonType()
        );

        if(foundGame.isFinalized()){
            throw new IllegalStateException("Game already finalized");
        }

        int homeTeamId = teamRepository.findTeamIdByNameAndYear(summary.getHomeTeam(), summary.getSeasonYear());
        int awayTeamId = teamRepository.findTeamIdByNameAndYear(summary.getAwayTeam(), summary.getSeasonYear());

        //Insert Goals
        for(GoalEntity goal : summary.getHomeGoals()){
            goal.setGame(foundGame);
            populateGoalEntity(goal, homeTeamId);
            statsRepository.insertGoalAndAssists(goal);
        }
        for(GoalEntity goal : summary.getAwayGoals()){
            goal.setGame(foundGame);
            populateGoalEntity(goal, awayTeamId);
            statsRepository.insertGoalAndAssists(goal);
        }

        //Insert Penalties
        for(PenaltyEntity penalty : summary.getHomePenalties()){
            penalty.setGame(foundGame);
            populatePenaltyEntity(penalty, homeTeamId);
            statsRepository.insertPenalty(penalty);
        }
        for(PenaltyEntity penalty : summary.getAwayPenalties()){
            penalty.setGame(foundGame);
            populatePenaltyEntity(penalty, awayTeamId);
            statsRepository.insertPenalty(penalty);
        }

        //Update game entity
        foundGame.setRefereeNameOne(summary.getRefereeOne());
        foundGame.setRefereeNameTwo(summary.getRefereeTwo());
        foundGame.setRefereeNameThree(summary.getRefereeThree());
        foundGame.setRefereeNotes(summary.getRefereeNotes());
        foundGame.setFinalized(true);
        foundGame.setFinalizedAt(LocalDateTime.now());
        gameRepository.updateGame(foundGame);

        return foundGame.getId();
    }

    private void populateGoalEntity(GoalEntity goalEntity, int teamId) {
        //Set the player id for goal, primary and secondary assists from players table
        int scorerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goalEntity.getJerseyNumber(), teamId);
        goalEntity.setPlayer(new PlayerEntity(scorerId));

        if(goalEntity.getPrimaryAssistJerseyNumber() != null){
            int primaryAssistPlayerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goalEntity.getPrimaryAssistJerseyNumber(), teamId);
            goalEntity.setPrimaryAssistPlayer(new PlayerEntity(primaryAssistPlayerId));
        }
        if(goalEntity.getSecondaryAssistJerseyNumber() != null){
            int secondaryAssistPlayerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goalEntity.getSecondaryAssistJerseyNumber(), teamId);
            goalEntity.setSecondaryAssistPlayer(new PlayerEntity(secondaryAssistPlayerId));
        }
    }

    private void populatePenaltyEntity(PenaltyEntity penaltyEntity, int teamId) {
        //set the player id for penalty from roster assignments from players table
        int playerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(penaltyEntity.getJerseyNumber(), teamId);
        penaltyEntity.setPlayer(new PlayerEntity(playerId));
    }

    private void populateAttendanceEntity(AttendanceEntity attendanceEntity, int teamId) {
        int playerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(attendanceEntity.getJerseyNumber(), teamId);
        attendanceEntity.setPlayer(new PlayerEntity(playerId));
        attendanceEntity.setTeam(new TeamEntity(teamId));
    }

    //TODO verify this works correctly before using in prod
    @Transactional
    public AttendanceUploadResult addAttendanceReport(AttendanceDto attendance) {
        GameEntity foundGame = gameRepository.findById(attendance.getGameId()).orElseThrow();

        //Handle insertion of attendance entities
        for(AttendanceUploadResult.AttendanceDetail attendanceDetail : attendance.getAttendanceDetails()){
            AttendanceEntity entity = new AttendanceEntity();
            entity.setGame(foundGame);
            entity.setJerseyNumber(attendanceDetail.getJerseyNumber());
            entity.setAttended(attendanceDetail.isAttended());

            populateAttendanceEntity(entity, attendance.getTeamId());
            //attendanceRepository.insertAttendanceEntity(entity);
        }

        //Create result object
        String teamName = teamRepository.findTeamNameById(attendance.getTeamId()).orElseThrow();

        AttendanceUploadResult result = new AttendanceUploadResult();
        result.setSeasonYear(foundGame.getYear());
        result.setSeasonType(foundGame.getSeasonType());
        result.setGameTime(foundGame.getGameTime());
        result.setTeamName(teamName);
        result.setAttendanceDetails(attendance.getAttendanceDetails());

        //Create a csv file to upload
        byte[] fileContent = createCsvFromAttendanceDetails(result);
        String fileName = result.getTeamName()
                + "-game"
                + attendance.getGameId()
                + "-attendance"
                + "-generated"
                + ".csv";
        GameUploadData uploadFileData = new GameUploadData(fileName, fileContent);
        uploadFileData.setGameId(foundGame.getId());
        uploadFileData.setYear(foundGame.getYear());
        //attendanceRepository.insertAttendanceUpload(uploadFileData);

        result.setAttendanceFileId(uploadFileData.getId());
        result.setAttendanceFileName(uploadFileData.getFileName());

        return result;
    }

    private byte[] createCsvFromAttendanceDetails(AttendanceUploadResult result) {
        StringBuilder sb = new StringBuilder();

        //Add game info
        sb.append("Team,Date,Season Type/Year,Attended\n")
            .append(result.getTeamName()).append(",")
            .append(result.getGameTime().toLocalDate().toString()).append(",")
            .append(result.getSeasonType().name()).append("-").append(result.getSeasonYear()).append("\n");

        //Add attendance details
        sb.append("#,Player Name,Attending\n");
        for(AttendanceUploadResult.AttendanceDetail detail : result.getAttendanceDetails()){
            sb.append(detail.getJerseyNumber()).append(",")
                    .append(detail.getPlayerName()).append(",")
                    .append(detail.isAttended() ? "Yes" : "No")
                    .append("\n");
        }

        //Remove the last newline
        if(!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString().getBytes();
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
        attendanceEntity.setPlayer(new PlayerEntity(payload.getPlayerId()));
        attendanceEntity.setTeam(new TeamEntity(payload.getTeamId()));
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

        int offenderId = playerRepository.findPlayerIdByJerseyNumberAndTeam(penalty.getJerseyNumber(), teamId);
        penaltyEntity.setGame(game);
        penaltyEntity.setPlayer(new PlayerEntity(offenderId));
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
        //Set the player id for goal, primary and secondary assists from players table
        int scorerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goal.getScorer(), teamId);
        goalEntity.setPlayer(new PlayerEntity(scorerId));

        if(goal.getAssist1() != null){
            int primaryAssistPlayerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goal.getAssist1(), teamId);
            goalEntity.setPrimaryAssistPlayer(new PlayerEntity(primaryAssistPlayerId));
        }
        if(goal.getAssist2() != null){
            int secondaryAssistPlayerId = playerRepository.findPlayerIdByJerseyNumberAndTeam(goal.getAssist2(), teamId);
            goalEntity.setSecondaryAssistPlayer(new PlayerEntity(secondaryAssistPlayerId));
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
}
