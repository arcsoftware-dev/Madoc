package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.exception.ResultNotFoundException;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.model.entity.GameEntity;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.repository.GameRepository;
import dev.arcsoftware.madoc.repository.RosterRepository;
import dev.arcsoftware.madoc.util.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class DocumentService {

    private final static String SIGN_IN_SHEET_TEMPLATE_RESOURCE_PATH = "data/templates/SignInSheetTemplate.ods";
    private final static String GAMESHEET_TEMPLATE_RESOURCE_PATH = "data/templates/GamesheetTemplate.ods";
    private final static String PREGAME_PACKAGE_TEMPLATE_RESOURCE_PATH = "data/templates/PreGamePackage.ods";


    private final GameRepository gameRepository;
    private final RosterRepository rosterRepository;

    @Autowired
    public DocumentService(GameRepository gameRepository, RosterRepository rosterRepository) {
        this.gameRepository = gameRepository;
        this.rosterRepository = rosterRepository;
    }

    public Pair<String, byte[]> getEmptyGamesheet(int gameId) {
        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResultNotFoundException("Game with ID "+gameId+" does not exist"));

        List<List<RosterAssignmentDto>> rosters = getRosters(gameEntity);

        byte[] data = getPopulatedGamesheet(gameEntity, rosters.getFirst(), rosters.getLast());
        if(data == null){
            throw new RuntimeException("Unable to export gamesheet data for game "+gameId);
        }

        String downloadFileName = "Gamesheet-"+gameEntity.getHomeTeam().getTeamName()+"-vs-"+gameEntity.getAwayTeam().getTeamName()+"-"+gameEntity.getGameTime().toString();
        return new Pair<>(downloadFileName, data);
    }

    private List<List<RosterAssignmentDto>> getRosters(GameEntity gameEntity){
        String homeTeam = gameEntity.getHomeTeam().getTeamName();
        List<RosterAssignmentDto> homeRoster = rosterRepository.getAssignmentsByYearAndTeam(gameEntity.getYear(), homeTeam);
        if(CollectionUtils.isEmpty(homeRoster)){
            throw new ResultNotFoundException("No roster found for team "+homeTeam+" for game id "+gameEntity.getId());
        }
        homeRoster.sort(Comparator.comparing(RosterAssignmentDto::getDraftPosition));

        String awayTeam = gameEntity.getAwayTeam().getTeamName();
        List<RosterAssignmentDto> awayRoster = rosterRepository.getAssignmentsByYearAndTeam(gameEntity.getYear(), awayTeam);
        if(CollectionUtils.isEmpty(awayRoster)){
            throw new ResultNotFoundException("No roster found for team "+awayTeam+" for game id "+gameEntity.getId());
        }
        awayRoster.sort(Comparator.comparing(RosterAssignmentDto::getDraftPosition));

        return List.of(homeRoster, awayRoster);
    }


    public Pair<String, byte[]> getEmptySignInSheet(int gameId, String teamName) {
        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResultNotFoundException("Game with ID "+gameId+" does not exist"));
        String normalizedTeamName = Utils.normalizeTeamName(teamName);

        if(!gameEntity.getHomeTeam().getTeamName().equals(normalizedTeamName) && !gameEntity.getAwayTeam().getTeamName().equals(normalizedTeamName)){
            throw new ResultNotFoundException("Team "+normalizedTeamName+" did not play in game "+gameId);
        }

        List<RosterAssignmentDto> roster = rosterRepository.getAssignmentsByYearAndTeam(gameEntity.getYear(), normalizedTeamName);
        if(CollectionUtils.isEmpty(roster)){
            throw new ResultNotFoundException("No roster found for team "+teamName+" for game id "+gameId);
        }
        roster.sort(Comparator.comparing(RosterAssignmentDto::getDraftPosition));

        byte[] data = getPopulatedSignInSheet(normalizedTeamName, gameEntity.getGameTime(), roster);
        if(data == null){
            throw new RuntimeException("Unable to export sign in sheet data for game "+gameId+" and team "+teamName);
        }

        String downloadFileName = "SignInSheet-"+teamName+"-"+gameEntity.getGameTime().toString();
        return new Pair<>(downloadFileName, data);
    }

    public Pair<String, byte[]> getPregamePackage(LocalDate date){
        List<GameEntity> games = gameRepository.findByDate(date);
        if(CollectionUtils.isEmpty(games)){
            throw new IllegalArgumentException("No Games available for "+date);
        }

        Resource resource = new ClassPathResource(PREGAME_PACKAGE_TEMPLATE_RESOURCE_PATH);
        byte[] documentData;
        try (InputStream inputStream = resource.getInputStream(); OdfDocument template = OdfDocument.loadDocument(inputStream)) {
            PreGamePackageData packageData = new PreGamePackageData();
            List<List<RosterAssignmentDto>> rosters = null;
            for(int i=0; i<games.size(); i++){
                GameEntity game = games.get(i);
                rosters = getRosters(game);
                if(CollectionUtils.isEmpty(rosters)){
                    throw new IllegalArgumentException("No Rosters available for game "+game.getId());
                }

                // Get first table sheet of the current document
                OdfTable gameSheet = template.getTableList(true).get(i*3);
                writeEmptyGamesheetData(gameSheet, game, rosters.getFirst(), rosters.getLast());

                OdfTable signInSheetHome = template.getTableList(true).get(i*3+1);
                writeEmptySigninSheetData(signInSheetHome, game.getHomeTeam().getTeamName(), game.getGameTime(), rosters.getFirst());

                OdfTable signInSheetAway = template.getTableList(true).get(i*3+2);
                writeEmptySigninSheetData(signInSheetAway, game.getHomeTeam().getTeamName(), game.getGameTime(), rosters.getLast());
            }
            documentData = exportDocument(template);
        } catch (Exception e) {
            log.error("Error reading pregame package template resource: {}", e.getMessage());
            throw new RuntimeException("Failed to load pregame package resource", e);
        }

        String dateString = DateTimeFormatter.BASIC_ISO_DATE.format(date);
        String downloadFileName = "PregamePackage-"+dateString;
        return new Pair<>(downloadFileName, documentData);
    }

    private byte[] exportDocument(OdfDocument template){
        byte[] data = null;
        ByteArrayInputStream returnStream = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            template.save(outputStream);

            returnStream = new ByteArrayInputStream(outputStream.toByteArray());
            data = returnStream.readAllBytes();
        } catch (Exception e){
            log.error("Error has occurred saving the document to an output stream");
        } finally {
            try{
                assert returnStream != null;
                returnStream.close();
            } catch (Exception e) {
                log.warn("Could not close the return stream.", e);
            }
        }
        return data;
    }

    private byte[] getPopulatedSignInSheet(String teamName, LocalDateTime gameTime, List<RosterAssignmentDto> roster){
        Resource resource = new ClassPathResource(SIGN_IN_SHEET_TEMPLATE_RESOURCE_PATH);
        try (InputStream inputStream = resource.getInputStream(); OdfDocument template = OdfDocument.loadDocument(inputStream)) {
            // Get first table sheet of the current document
            OdfTable signInSheet = template.getTableList(true).getFirst();

            writeEmptySigninSheetData(signInSheet, teamName, gameTime, roster);

            return exportDocument(template);
        } catch (Exception e) {
            log.error("Error reading signin sheet template resource: {}", e.getMessage());
            throw new RuntimeException("Failed to load signin sheet template resource", e);
        }
    }

    private byte[] getPopulatedGamesheet(GameEntity gameEntity, List<RosterAssignmentDto> homeRoster, List<RosterAssignmentDto> awayRoster){
        Resource resource = new ClassPathResource(GAMESHEET_TEMPLATE_RESOURCE_PATH);
        try (InputStream inputStream = resource.getInputStream(); OdfDocument template = OdfDocument.loadDocument(inputStream)) {
            // Get first table sheet of the current document
            OdfTable gamesheet = template.getTableList(true).getFirst();

            writeEmptyGamesheetData(gamesheet, gameEntity, homeRoster, awayRoster);

            return exportDocument(template);
        } catch (Exception e) {
            log.error("Error reading gamesheet template resource: {}", e.getMessage());
            throw new RuntimeException("Failed to load gamesheet template resource", e);
        }
    }

    private void writeEmptyGamesheetData(OdfTable gamesheet, GameEntity gameEntity, List<RosterAssignmentDto> homeRoster, List<RosterAssignmentDto> awayRoster){
        //Write date and season type (A1)
        StringBuilder dateBuilder = new StringBuilder()
                .append("Madoc Mens Hockey League ")
                .append(gameEntity.getYear())
                .append("/")
                .append(gameEntity.getYear()+1)
                .append("\n")
                .append(gameEntity.getSeasonType().getLabel());
        gamesheet.getCellByPosition(0,0).setStringValue(dateBuilder.toString());

        //Write venue (A3)
        gamesheet.getCellByPosition(0,2).setStringValue(gameEntity.getVenue().getArenaName());

        //Write Date (C2), Time (C3)
        String gameDate = gameEntity.getGameTime().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        gamesheet.getCellByPosition(2,1).setStringValue(gameDate);

        String gameTime = gameEntity.getGameTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH));
        gamesheet.getCellByPosition(2,2).setStringValue(gameTime);

        //Write Home Team Name (B5), HomeTeam Roster Jerseys(A7:A21), Names (B7:B21), Goalie always at 21
        gamesheet.getCellByPosition(1,4).setStringValue(gameEntity.getHomeTeam().getTeamName());
        writeRosterToTable(gamesheet, 0, 1, 6, homeRoster);

        //Write Away Team Name (B24), HomeTeam Roster Jerseys(A26:A40), Names (B26:B40), Goalie always at 40
        gamesheet.getCellByPosition(1,23).setStringValue(gameEntity.getAwayTeam().getTeamName());
        writeRosterToTable(gamesheet, 0, 1, 25, awayRoster);
    }

    private void writeRosterToTable(OdfTable template, int numberColumnIndex, int nameColumnIndex, int rowIndexStart, List<RosterAssignmentDto> roster){
        OdfTableColumn numbersColumn = template.getColumnByIndex(numberColumnIndex);
        OdfTableColumn namesColumn = template.getColumnByIndex(nameColumnIndex);
        for(int playerIndex=0, rowIndex = rowIndexStart; playerIndex < roster.size(); playerIndex++, rowIndex++){
            var player = roster.get(playerIndex);
            if(!player.isActive()){
                rowIndex--;
                continue;
            }
            String number = player.getJerseyNumber() == 0 ? "" : String.valueOf(player.getJerseyNumber());
            numbersColumn.getCellByIndex(rowIndex).setStringValue(number);
            namesColumn.getCellByIndex(rowIndex).setStringValue(player.getFullName());
        }
    }

    private void writeEmptySigninSheetData(OdfTable signInSheet, String teamName, LocalDateTime gameTime, List<RosterAssignmentDto> roster){
        // Write data
        //Team name and date (C1, F1)
        signInSheet.getCellByPosition(2,0).setStringValue(teamName);
        signInSheet.getCellByPosition(5, 0).setStringValue(gameTime.toString());

        // Jersey numbers and names (A6:21, B6:21)
        writeRosterToTable(signInSheet, 0, 1, 5, roster);
    }

    @Getter
    private static class PreGamePackageData{
        private final List<List<RosterAssignmentDto>> rosters = new ArrayList<>(6);
        private final List<GameEntity> games = new ArrayList<>(3);

        public void addRoster(List<RosterAssignmentDto> roster){
            this.rosters.add(roster);
        }

        public void addGame(GameEntity game){
            this.games.add(game);
        }
    }
}
