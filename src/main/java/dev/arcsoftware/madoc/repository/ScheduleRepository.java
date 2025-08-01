package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ScheduleRepository {
    public List<ScheduleItemDto> getUpcomingMatches() {
        // This method should return a list of upcoming matches.
        // For now, we return an empty list as a placeholder.
        return List.of(
                new ScheduleItemDto(
                        LocalDateTime.of(2025, 10, 1, 19, 0),
                        "Whalers",
                        "Leafs"
                ),
                new ScheduleItemDto(
                        LocalDateTime.of(2025, 10, 1, 20, 0),
                        "Vegas",
                        "Redwings"
                ),
                new ScheduleItemDto(
                        LocalDateTime.of(2025, 10, 1, 21, 0),
                        "Avalanche",
                        "Blackhawks"
                )
        );
    }
}
