package dev.arcsoftware.madoc.util;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.GoalType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.model.csv.RosterUploadRow;
import dev.arcsoftware.madoc.model.csv.ScheduleUploadRow;
import dev.arcsoftware.madoc.model.entity.GoalEntity;
import dev.arcsoftware.madoc.model.entity.PenaltyEntity;
import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.model.payload.AttendanceUploadResult;
import dev.arcsoftware.madoc.model.payload.GamesheetSummary;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileUploadParser {
    public List<RosterUploadRow> parseRosterCsv(byte[] csvFileBytes) {
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
                        Utils.toCamelCase(record.get("Player")),
                        Utils.toCamelCase(record.get("Team")),
                        DraftRank.valueOf(record.get("Draft Rank").toUpperCase()),
                        record.get("isRookie").equalsIgnoreCase("true")
                );
                rows.add(rosterUploadRow);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rows;
    }

    public List<ScheduleUploadRow> parseScheduleCsv(byte[] csvFileBytes) {
        List<ScheduleUploadRow> rows = new ArrayList<>();

        final String[] HEADERS = {"Datetime", "Home Team", "Away Team"};
        try(Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFileBytes))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                ScheduleUploadRow scheduleUploadRow = new ScheduleUploadRow(
                        LocalDateTime.parse(record.get("Datetime")),
                        Utils.toCamelCase(record.get("Home Team")),
                        Utils.toCamelCase(record.get("Away Team"))
                );
                rows.add(scheduleUploadRow);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rows;
    }

    //TODO: Clean up this holy grail of a mess
    public GamesheetSummary parseGamesheet(byte[] csvFileBytes){
        GamesheetSummary summary = new GamesheetSummary();
        summary.setHomeGoals(new ArrayList<>());
        summary.setHomePenalties(new ArrayList<>());
        summary.setAwayGoals(new ArrayList<>());
        summary.setAwayPenalties(new ArrayList<>());
        summary.setRefereeNotes(new ArrayList<>());

        String csvFileString = new String(csvFileBytes, StandardCharsets.UTF_8);
        String[] lines = csvFileString.split("\n");

        //Process Header info
        for (String line : lines) {
            if (line.startsWith("DateTime")) {
                continue;
            }
            else if (line.startsWith(",")) {
                break;
            }
            //We have the data row now
            String[] parts = line.split(",");

            summary.setGameTime(LocalDateTime.parse(parts[0].trim()));
            summary.setHomeTeam(parts[1].trim());
            summary.setAwayTeam(parts[2].trim());
            summary.setVenue(Arena.valueOf(parts[3].trim().toUpperCase()));
            summary.setSeasonYear(Integer.parseInt(parts[6].trim()));
            summary.setSeasonType(SeasonType.valueOf(parts[7].trim().toUpperCase()));
        }

        //Process Goals
        boolean foundGoals = false;
        for (String line : lines) {
            if (line.startsWith("Goal")) {
                foundGoals = true;
                continue;
            }
            else if (line.startsWith("Home Team Penalties")) {
                break;
            }
            else if(!foundGoals) {
                continue;
            }
            //We have the start of the data
            String[] parts = line.split(",");

            //Create home goal if in row
            if(parts.length>1 && !parts[0].trim().isBlank()){
                Integer scorer = Integer.parseInt(parts[0].trim());
                String pAssistStr = parts[1].trim();
                String sAssistStr = parts[2].trim();
                int period = Integer.parseInt(parts[3].trim());
                String[] timeParts = parts[4].trim().split(":");
                Pair<String, String> timePair = new Pair<>(timeParts[0], timeParts[1]);
                GoalType goalType;
                if(parts.length > 5) {
                    goalType = GoalType.fromCode(parts[5].trim().toUpperCase());
                }
                else{
                    goalType = GoalType.REGULAR;
                }

                GoalEntity homeGoal = new GoalEntity();
                homeGoal.setJerseyNumber(scorer);
                if(!pAssistStr.isBlank()) homeGoal.setPrimaryAssistJerseyNumber(Integer.parseInt(pAssistStr));
                if(!sAssistStr.isBlank()) homeGoal.setSecondaryAssistJerseyNumber(Integer.parseInt(sAssistStr));
                homeGoal.setPeriod(period);
                homeGoal.setGoalType(goalType);
                homeGoal.setTime(LocalTime.of(0, Integer.parseInt(timePair.left()), Integer.parseInt(timePair.right())));
                summary.addHomeGoal(homeGoal);
            }
            //Create away goal if in row
            if(parts.length>6 && !parts[6].trim().isBlank()){
                Integer scorer = Integer.parseInt(parts[6].trim());
                String pAssistStr = parts[7].trim();
                String sAssistStr = parts[8].trim();
                int period = Integer.parseInt(parts[9].trim());
                String[] timeParts = parts[10].trim().split(":");
                Pair<String, String> timePair = new Pair<>(timeParts[0], timeParts[1]);
                GoalType goalType;
                if(parts.length > 11) {
                    goalType = GoalType.fromCode(parts[11].trim().toUpperCase());
                }
                else{
                    goalType = GoalType.REGULAR;
                }

                GoalEntity awayGoal = new GoalEntity();
                awayGoal.setJerseyNumber(scorer);
                if(!pAssistStr.isBlank()) awayGoal.setPrimaryAssistJerseyNumber(Integer.parseInt(pAssistStr));
                if(!sAssistStr.isBlank()) awayGoal.setSecondaryAssistJerseyNumber(Integer.parseInt(sAssistStr));
                awayGoal.setPeriod(period);
                awayGoal.setGoalType(goalType);
                awayGoal.setTime(LocalTime.of(0, Integer.parseInt(timePair.left()), Integer.parseInt(timePair.right())));
                summary.addAwayGoal(awayGoal);
            }
        }
        if(summary.getHomeGoals() != null){
            summary.setHomeScore(summary.getHomeGoals().size());
        }
        if(summary.getAwayGoals() != null){
            summary.setAwayScore(summary.getAwayGoals().size());
        }

        //Process Penalties
        boolean foundPenalties = false;
        for (String line : lines) {
            if (line.startsWith("Player")) {
                foundPenalties = true;
                continue;
            }
            else if (line.startsWith("Referee One")) {
                break;
            }
            else if(!foundPenalties) {
                continue;
            }
            //We have the start of the data
            String[] parts = line.split(",");

            //Create home penalty if in row
            if(parts.length > 1 && !parts[0].trim().isBlank()){
                Integer player = Integer.parseInt(parts[0].trim());
                int minutes = Integer.parseInt(parts[1].trim());
                String infraction = parts[2].trim();
                int period = Integer.parseInt(parts[3].trim());
                String[] timeParts = parts[4].trim().split(":");
                Pair<String, String> timePair = new Pair<>(timeParts[0], timeParts[1]);

                PenaltyEntity homePenalty = new PenaltyEntity();
                homePenalty.setJerseyNumber(player);
                homePenalty.setMinutes(minutes);
                homePenalty.setInfraction(infraction);
                homePenalty.setPeriod(period);
                homePenalty.setTime(LocalTime.of(0, Integer.parseInt(timePair.left()), Integer.parseInt(timePair.right())));
                summary.addHomePenalty(homePenalty);
            }
            //Create away penalty if in row
            if(parts.length>6 && !parts[6].trim().isBlank()){
                Integer player = Integer.parseInt(parts[6].trim());
                int minutes = Integer.parseInt(parts[7].trim());
                String infraction = parts[8].trim();
                int period = Integer.parseInt(parts[9].trim());
                String[] timeParts = parts[10].trim().split(":");
                Pair<String, String> timePair = new Pair<>(timeParts[0], timeParts[1]);

                PenaltyEntity awayPenalty = new PenaltyEntity();
                awayPenalty.setJerseyNumber(player);
                awayPenalty.setMinutes(minutes);
                awayPenalty.setInfraction(infraction);
                awayPenalty.setPeriod(period);
                awayPenalty.setTime(LocalTime.of(0, Integer.parseInt(timePair.left()), Integer.parseInt(timePair.right())));
                summary.addAwayPenalty(awayPenalty);
            }
        }
        if(summary.getHomePenalties() != null){
            summary.setHomePenaltyMinutes(summary.getHomePenalties().stream().mapToInt(PenaltyEntity::getMinutes).sum());
        }
        if(summary.getAwayPenalties() != null){
            summary.setAwayPenaltyMinutes(summary.getAwayPenalties().stream().mapToInt(PenaltyEntity::getMinutes).sum());
        }

        //Process Referee Names
        boolean foundRefNames = false;
        for (String line : lines) {
            if (line.startsWith("Referee One")) {
                foundRefNames = true;
                continue;
            }
            else if (foundRefNames && line.startsWith(",")) {
                break;
            }
            else if (!foundRefNames){
                continue;
            }

            //We have the data row now
            String[] parts = line.split(",");

            if(parts.length > 0){
                summary.setRefereeOne(parts[0].trim());
            }
            if(parts.length > 1){
                String refName2 = parts[1].trim();
                if(!refName2.isBlank()) summary.setRefereeTwo(refName2);
            }
            if(parts.length > 2){
                String refName3 = parts[2].trim();
                if(!refName3.isBlank()) summary.setRefereeThree(refName3);
            }

            break;
        }

        //Process Referee Notes
        boolean foundRefNotes = false;
        for (String line : lines) {
            if (line.startsWith("Referee Notes")) {
                foundRefNotes = true;
                continue;
            }
            else if (foundRefNotes && line.startsWith(",")) {
                break;
            }
            else if (!foundRefNotes) {
                continue;
            }

            //We have the data row now
            String[] parts = line.split(",");
            if(parts.length == 0) break;
            String note = parts[0].trim();
            if (!note.isBlank()) summary.addRefNote(note);
        }
        return summary;
    }


    public AttendanceUploadResult parseAttendanceSheet(byte[] fileContent) {
        AttendanceUploadResult result = new AttendanceUploadResult();
        result.setAttendanceDetails(new ArrayList<>());

        String csvFileString = new String(fileContent, StandardCharsets.UTF_8);
        String[] lines = csvFileString.split("\n");

        //Parse Metadata
        String[] metaDataParts = lines[1].split(",");
        result.setTeamName(metaDataParts[0].trim());
        result.setGameTime(LocalDateTime.parse(metaDataParts[1].trim()));
        String[] seasonInfo = metaDataParts[2].split("-");
        result.setSeasonType(SeasonType.valueOf(seasonInfo[0].trim().toUpperCase()));
        result.setSeasonYear(Integer.parseInt(seasonInfo[1].trim()));

        //Parse
        for(int l=3; l<lines.length; l++){
            String[] data = lines[l].split(",");
            AttendanceUploadResult.AttendanceDetail attendanceDetail = new AttendanceUploadResult.AttendanceDetail();
            attendanceDetail.setJerseyNumber(Integer.parseInt(data[0].trim()));
            attendanceDetail.setPlayerName(data[1].trim());
            boolean attended = data.length > 2 && !data[2].trim().isBlank();
            attendanceDetail.setAttended(attended);
            result.addAttendanceDetail(attendanceDetail);
        }

        return result;
    }

    public List<RuleEntity> parseRuleFile(byte[] csvFileBytes) {
        List<RuleEntity> rules = new ArrayList<>();

        final String[] HEADERS = {"Title", "Description"};
        try(Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFileBytes))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                RuleEntity ruleEntity = new RuleEntity();
                ruleEntity.setTitle(record.get("Title"));
                ruleEntity.setDescription(record.get("Description"));

                rules.add(ruleEntity);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rules;
    }
}
