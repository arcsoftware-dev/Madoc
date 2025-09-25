package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.csv.ScheduleUploadRow;
import dev.arcsoftware.madoc.model.entity.GameEntity;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.model.payload.ScheduleUploadResult;
import dev.arcsoftware.madoc.repository.GameRepository;
import dev.arcsoftware.madoc.util.FileUploadParser;
import dev.arcsoftware.madoc.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScheduleService {
    private final GameRepository gameRepository;

    private final SeasonMetadataService seasonMetadataService;
    private final TeamsService teamsService;
    private final FileUploadParser fileUploadParser;

    @Autowired
    public ScheduleService(GameRepository gameRepository,
                           SeasonMetadataService seasonMetadataService,
                           TeamsService teamsService,
                           FileUploadParser fileUploadParser
    ) {
        this.gameRepository = gameRepository;
        this.seasonMetadataService = seasonMetadataService;
        this.teamsService = teamsService;
        this.fileUploadParser = fileUploadParser;
    }

    @Transactional
    public ScheduleUploadResult uploadSchedule(UploadFileData uploadFileData, SeasonType seasonType) {
        List<ScheduleUploadRow> scheduleUploadRows = fileUploadParser.parseScheduleCsv(uploadFileData.getFileContent());
        log.info("Parsed rows from CSV {}", scheduleUploadRows);

        Set<String> uniqueTeamNamesFromSchedule = scheduleUploadRows.stream()
                .flatMap(up -> Stream.of(up.getHomeTeam(), up.getAwayTeam()))
                .map(Utils::toCamelCase)
                .collect(Collectors.toSet());

        List<TeamEntity> teamEntities = teamsService.getAndCreateTeamsIfNotFound(uploadFileData.getYear(), uniqueTeamNamesFromSchedule);

        Map<String, TeamEntity> teamEntitiesMap = teamEntities.stream()
                .collect(Collectors.toMap(TeamEntity::getTeamName, teamEntity -> teamEntity));

        //Build Game entities
        List<GameEntity> games = new ArrayList<>();
        for(ScheduleUploadRow scheduleUploadRow : scheduleUploadRows){
            GameEntity gameEntity = new GameEntity();
            gameEntity.setGameTime(scheduleUploadRow.getGameTime());
            gameEntity.setHomeTeam(teamEntitiesMap.get(scheduleUploadRow.getHomeTeam()));
            gameEntity.setAwayTeam(teamEntitiesMap.get(scheduleUploadRow.getAwayTeam()));
            gameEntity.setYear(uploadFileData.getYear());
            gameEntity.setSeasonType(seasonType);
            gameEntity.setVenue(scheduleUploadRow.getArena());
            games.add(gameEntity);
        }

        //Insert Game entities
        for(GameEntity createdGame : games){
            this.gameRepository.insertGame(createdGame);
        }

        //Convert uploaded game entities to scheduleItemDtos
        List<ScheduleItemDto> scheduleItemDtos = games.stream()
                .map(game -> new ScheduleItemDto(
                        game.getGameTime(),
                        game.getHomeTeam().getTeamName(),
                        game.getAwayTeam().getTeamName(),
                        null,
                        null
                ))
                .toList();

        log.info("Uploading schedule file data to the database");
        this.gameRepository.uploadScheduleFile(uploadFileData);

        return new ScheduleUploadResult(
                scheduleItemDtos,
                uploadFileData.getId(),
                uploadFileData.getYear(),
                uploadFileData.getFileName()
        );
    }

    public List<ScheduleItemDto> getUpcomingMatches(int limit) {
        var schedule = getSchedule(seasonMetadataService.getCurrentSeasonType(), seasonMetadataService.getCurrentSeasonYear());
        LocalDateTime now = LocalDate.now().atStartOfDay();
        return schedule.stream()
                .filter(item -> item.getStartTime().isAfter(now))
                .limit(limit)
                .toList();
    }

    public List<ScheduleItemDto> getSchedule(SeasonType seasonType, Integer year) {
        if (seasonType == null) {
            seasonType = seasonMetadataService.getCurrentSeasonType();
        }
        if (year == null) {
            year = seasonMetadataService.getCurrentSeasonYear();
        }
        return gameRepository.getSchedule(seasonType, year);
    }

    public List<GroupedScheduleDto> getGroupedSchedule(SeasonType seasonType, Integer year) {
        List<ScheduleItemDto> scheduleItemDtos = gameRepository.getSchedule(seasonType, year);
        return scheduleItemDtos.stream()
                .collect(Collectors.groupingBy(item -> item.getStartTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> new GroupedScheduleDto(entry.getKey(), entry.getValue()))
                .sorted(java.util.Comparator.comparing(GroupedScheduleDto::getDate))
                .toList();
    }

    public byte[] generatePrintableScheduleCsv(Integer year, SeasonType seasonType, String teamFilter) {
        List<GroupedScheduleDto> groupedScheduleDtos = getGroupedSchedule(seasonType, year);

        StringBuilder builder = new StringBuilder();
        builder.append("Date,Time,Home,Away").append("\n");
        for(GroupedScheduleDto groupedScheduleDto : groupedScheduleDtos) {
            for(ScheduleItemDto item : groupedScheduleDto.getGames()) {
                if(teamFilter == null
                        || teamFilter.equalsIgnoreCase("ALL")
                        || teamFilter.equalsIgnoreCase(item.getHomeTeam())
                        || teamFilter.equalsIgnoreCase(item.getAwayTeam())
                ) {
                    // Only include games with the specified team
                    builder.append(item.getStartTime().toLocalDate()).append(",");
                    builder.append(item.getStartTime().toLocalTime()).append(",");
                    builder.append(item.getHomeTeam()).append(",");
                    builder.append(item.getAwayTeam()).append("\n");
                }
            }
        }

        return builder.toString().getBytes();
    }
}
