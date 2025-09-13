package dev.arcsoftware.madoc.model.payload;

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
