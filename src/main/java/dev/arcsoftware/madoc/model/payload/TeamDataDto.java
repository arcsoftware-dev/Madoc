package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.timesheet.RosterDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeamDataDto {
    private String teamName;
    private List<RosterDto> roster;
    private TeamStatsDto teamStats;
    private List<StatsDto> playerStats;
    private List<StatsDto> goalieStats;

}
