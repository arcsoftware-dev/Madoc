package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.RosterFileData;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.request.RosterUploadRow;
import dev.arcsoftware.madoc.repository.PlayerRepository;
import dev.arcsoftware.madoc.repository.RosterRepository;
import dev.arcsoftware.madoc.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RosterService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final RosterRepository rosterRepository;

    @Autowired
    public RosterService(PlayerRepository playerRepository, TeamRepository teamRepository, RosterRepository rosterRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.rosterRepository = rosterRepository;
    }

    @Transactional
    public List<RosterAssignment> assignRosters(RosterFileData rosterFileData) {
        List<PlayerEntity> playerEntities = playerRepository.getPlayers();
        log.info("Found {} players", playerEntities.size());

        List<TeamEntity> teamEntities = teamRepository.getTeamsByYear(rosterFileData.getYear());
        log.info("Found {} teams", teamEntities.size());

        List<RosterUploadRow> rosterUploadRows = parseCsv(rosterFileData.getFileContent());
        log.info("Parsed rows from CSV {}", rosterUploadRows);

        List<RosterAssignment> rosterAssignments = createRosterAssignments(rosterUploadRows, playerEntities, teamEntities, rosterFileData.getYear());

        for(RosterAssignment rosterAssignment : rosterAssignments) {
            rosterRepository.insertRosterAssignment(rosterAssignment);
        }
        log.info("Roster assignments inserted into the database");

        log.info("Uploading roster file data to the database");
        this.rosterRepository.uploadRosterFile(rosterFileData);

        log.info("Successfully Loaded Rosters for Year: {}", rosterFileData.getYear());
        return rosterAssignments;
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

    private List<RosterUploadRow> parseCsv(byte[] csvFileBytes) {
        List<RosterUploadRow> rows = new ArrayList<>();

        final String[] HEADERS = {"#","Player","Team","Draft Rank","isRookie"};
        try(Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFileBytes))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                RosterUploadRow rosterUploadRow = new RosterUploadRow(
                        Integer.parseInt(record.get("#")),
                        record.get("Player"),
                        record.get("Team"),
                        DraftRank.valueOf(record.get("Draft Rank")),
                        record.get("isRookie").equalsIgnoreCase("true")
                );
                rows.add(rosterUploadRow);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rows;
    }
}
