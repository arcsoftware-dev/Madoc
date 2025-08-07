package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class ScheduleRepository {
    private final Map<String, List<ScheduleItemDto>> cachedSchedules = new ConcurrentHashMap<>();

    public List<ScheduleItemDto> getUpcomingMatches(SeasonType seasonType, int year) {
        List<ScheduleItemDto> schedule = getSchedule(seasonType, year);

        LocalDateTime now = LocalDate.now().atStartOfDay();
        return schedule.stream()
                .filter(item -> item.getStartTime().isAfter(now))
                .limit(3) // Limit to 3 upcoming matches
                .toList();
    }

    public List<ScheduleItemDto> getSchedule(SeasonType seasonType, int year) {
        String key = seasonType.name() + "_" + year;
        if (!cachedSchedules.containsKey(key)) {
            List<ScheduleItemDto> schedule = loadScheduleFromCsv(seasonType, year);
            cachedSchedules.put(key, schedule);
            return schedule;
        } else {
            return cachedSchedules.get(key);
        }
    }

    private List<ScheduleItemDto> loadScheduleFromCsv(SeasonType seasonType, int year) {
        List<ScheduleItemDto> schedule = new ArrayList<>();

        String scheduleFileName = String.format("data/schedule/%d/%s.csv", year, seasonType.name());
        ClassPathResource scheduleCsv = new ClassPathResource(scheduleFileName);

        final String[] HEADERS = {"Datetime","Home Team","Away Team"};
        try(Reader reader = new FileReader(scheduleCsv.getFile())) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                ScheduleItemDto scheduleItemDto = new ScheduleItemDto(
                        LocalDateTime.parse(record.get("Datetime")),
                        record.get("Home Team"),
                        record.get("Away Team")
                );
                schedule.add(scheduleItemDto);
            }
        } catch (IOException e) {
            log.error("Error loading schedule from CSV for {} {}", seasonType, year, e);
            return new ArrayList<>(); // Return empty list on error
        }
        return schedule;
    }
}
