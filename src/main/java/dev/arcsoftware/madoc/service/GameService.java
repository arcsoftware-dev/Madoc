package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.*;
import dev.arcsoftware.madoc.model.payload.AttendanceUploadResult;
import dev.arcsoftware.madoc.model.payload.GamesheetSummary;
import dev.arcsoftware.madoc.repository.*;
import dev.arcsoftware.madoc.util.FileUploadParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class GameService {
    private final StatsRepository statsRepository;
    private final GameRepository gameRepository;
    private final AttendanceRepository attendanceRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    private final FileUploadParser fileUploadParser;

    @Autowired
    public GameService(StatsRepository statsRepository,
                       GameRepository gameRepository,
                       AttendanceRepository attendanceRepository,
                       PlayerRepository playerRepository,
                       TeamRepository teamRepository,
                       FileUploadParser fileUploadParser
    ) {
        this.statsRepository = statsRepository;
        this.gameRepository = gameRepository;
        this.attendanceRepository = attendanceRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.fileUploadParser = fileUploadParser;
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
}
