package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, SeasonMetadataService seasonMetadataService) {
        this.scheduleRepository = scheduleRepository;
        this.seasonMetadataService = seasonMetadataService;
    }

    public List<ScheduleItemDto> getUpcomingMatches() {
        return scheduleRepository.getUpcomingMatches(seasonMetadataService.getCurrentSeasonType(), seasonMetadataService.getCurrentSeasonYear());
    }

    public List<ScheduleItemDto> getSchedule(SeasonType seasonType, Integer year) {
        if (seasonType == null) {
            seasonType = seasonMetadataService.getCurrentSeasonType();
        }
        if (year == null) {
            year = seasonMetadataService.getCurrentSeasonYear();
        }
        return scheduleRepository.getSchedule(seasonType, year);
    }

    public List<GroupedScheduleDto> getGroupedSchedule(SeasonType seasonType, Integer year) {
        List<ScheduleItemDto> scheduleItemDtos = scheduleRepository.getSchedule(seasonType, year);
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
