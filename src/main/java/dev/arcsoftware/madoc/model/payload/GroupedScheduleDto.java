package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Arena;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Data
public class GroupedScheduleDto {
    private LocalDate date;
    private Arena arena;
    private List<ScheduleItemDto> games;
}
