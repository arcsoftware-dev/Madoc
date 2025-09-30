package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.csv.RosterUploadRow;
import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.RosterUploadResult;
import dev.arcsoftware.madoc.repository.RosterRepository;
import dev.arcsoftware.madoc.util.FileUploadParser;
import dev.arcsoftware.madoc.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RosterService {

    private final PlayersService playersService;
    private final TeamsService teamsService;
    private final RosterRepository rosterRepository;
    private final FileUploadParser fileUploadParser;

    @Autowired
    public RosterService(PlayersService playersService,
                         TeamsService teamsService,
                         RosterRepository rosterRepository,
                         FileUploadParser fileUploadParser
    ) {
        this.playersService = playersService;
        this.teamsService = teamsService;
        this.rosterRepository = rosterRepository;
        this.fileUploadParser = fileUploadParser;
    }

    @Transactional
    public RosterUploadResult assignRosters(UploadFileData uploadFileData) {
        List<RosterUploadRow> rosterUploadRows = fileUploadParser.parseRosterCsv(uploadFileData.getFileContent());
        log.info("Parsed rows from CSV {}", rosterUploadRows);

        Set<String> uniquePlayerNamesFromRosters = rosterUploadRows.stream()
                .map(RosterUploadRow::getPlayer)
                .map(Utils::toCamelCase)
                .collect(Collectors.toSet());

        List<PlayerEntity> playerEntities = playersService.getAndCreatePlayersIfNotFound(uniquePlayerNamesFromRosters);

        Set<String> uniqueTeamNamesFromRosters = rosterUploadRows.stream()
                .map(RosterUploadRow::getTeam)
                .map(Utils::toCamelCase)
                .collect(Collectors.toSet());

        List<TeamEntity> teamEntities = teamsService.getAndCreateTeamsIfNotFound(uploadFileData.getYear(), uniqueTeamNamesFromRosters);

        List<RosterAssignment> rosterAssignments = createRosterAssignments(rosterUploadRows, playerEntities, teamEntities, uploadFileData.getYear());

        for(RosterAssignment rosterAssignment : rosterAssignments) {
            rosterRepository.insertRosterAssignment(rosterAssignment);
        }
        log.info("Roster assignments inserted into the database");

        log.info("Uploading roster file data to the database");
        this.rosterRepository.uploadRosterFile(uploadFileData);

        log.info("Successfully Loaded Rosters for Year: {}", uploadFileData.getYear());

        return new RosterUploadResult(
                rosterAssignments,
                uploadFileData.getId(),
                uploadFileData.getYear(),
                uploadFileData.getFileName()
        );
    }

    public List<RosterAssignmentDto> getAssignedRostersByYearAndTeam(int year, String teamName) {
        var assignments = rosterRepository.getAssignmentsByYearAndTeam(year, teamName);
        assignments.sort(Comparator.comparing(p -> p.getDraftPosition().getRank()));
        return assignments;
    }

    public List<List<RosterAssignmentDto>> getAssignedRostersByYear(Integer year) {
        List<RosterAssignmentDto> allAssignmentsForYear = rosterRepository.getAssignmentsByYear(year);
        var assignmentsByTeam = new ArrayList<>(allAssignmentsForYear
                .stream()
                .collect(Collectors.groupingBy(RosterAssignmentDto::getTeamId))
                .values());
        for(List<RosterAssignmentDto> teamAssignments : assignmentsByTeam) {
            teamAssignments.sort(Comparator.comparing(p -> p.getDraftPosition().getRank()));
        }
        return assignmentsByTeam;
    }

    private List<RosterAssignment> createRosterAssignments(
            List<RosterUploadRow> rosterUploadRows,
            List<PlayerEntity> playerEntities,
            List<TeamEntity> teamEntities,
            int year
    ) {
        List<RosterAssignment> rosterAssignments = new ArrayList<>();

        for(RosterUploadRow row : rosterUploadRows) {
            PlayerEntity player = playerEntities.stream()
                    .filter(p -> (p.getFirstName()+" "+p.getLastName()).equalsIgnoreCase(row.getPlayer()))
                    .findFirst()
                    .orElse(null);
            if(player == null) {
                log.error("Player not found: {}", row.getPlayer());
                throw new RuntimeException("Player not found: " + row.getPlayer());
            }

            TeamEntity team = teamEntities.stream()
                    .filter(t -> t.getTeamName().equalsIgnoreCase(row.getTeam()))
                    .findFirst()
                    .orElse(null);
            if(team == null) {
                log.warn("Team not found: {}", row.getTeam());
                throw new RuntimeException("Team not found: " + row.getTeam());
            }

            RosterAssignment rosterAssignment = new RosterAssignment();
            rosterAssignment.setPlayerId(player.getId());
            rosterAssignment.setTeamId(team.getId());
            rosterAssignment.setSeasonYear(year);
            rosterAssignment.setDraftPosition(row.getDraftRank());
            rosterAssignment.setPosition(Position.fromDraftRank(row.getDraftRank()));
            rosterAssignment.setJerseyNumber(row.getNumber());
            rosterAssignment.setRookie(row.isRookie());

            rosterAssignments.add(rosterAssignment);
        }

        return rosterAssignments;
    }

    public void modifyRosters(List<RosterAssignmentDto> rosterAssignments) {
        for(RosterAssignment rosterAssignment : rosterAssignments) {
            rosterRepository.updateAssignment(rosterAssignment);
        }
    }

    public void addPlayerToRoster(RosterAssignmentDto rosterAssignment) {
        //Look up player and team to ensure they exist
        if(!teamsService.teamExistsById(rosterAssignment.getTeamId())) {
            throw new RuntimeException("Team not found with ID: " + rosterAssignment.getTeamId());
        }
        if(!playersService.playerExistsById(rosterAssignment.getPlayerId())) {
            throw new RuntimeException("Player not found with ID: " + rosterAssignment.getPlayerId());
        }

        rosterRepository.insertRosterAssignment(rosterAssignment);
    }

    public void addPlayer(PlayerEntity playerEntity) {
        playersService.insertNewPlayer(playerEntity);
    }
}
