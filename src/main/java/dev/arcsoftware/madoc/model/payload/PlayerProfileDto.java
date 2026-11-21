package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlayerProfileDto {
    private final Integer playerId;
    private final String fullName;
    private final String currentTeam;
    private final Integer currentSeasonYear;
    private final Integer currentJerseyNumber;
    private final Position currentPosition;
    private final DraftRank currentDraftPosition;
    private final boolean currentRookie;
    private final boolean primaryGoalie;
    private final int seasonsPlayed;

    private final List<RosterAssignmentDto> rosterHistory;

    private final List<PlayerSeasonStat> skaterRegularStats;
    private final List<PlayerSeasonStat> skaterPlayoffStats;
    private final PlayerSeasonStat skaterRegularTotals;
    private final PlayerSeasonStat skaterPlayoffTotals;

    private final List<PlayerSeasonStat> goalieRegularStats;
    private final List<PlayerSeasonStat> goaliePlayoffStats;
    private final PlayerSeasonStat goalieRegularTotals;
    private final PlayerSeasonStat goaliePlayoffTotals;

    private final List<PlayerGameLogDto> gameLogs;
    private final int totalGameLogs;

    @Getter
    @Builder
    public static class PlayerSeasonStat {
        private final int year;
        private final String teamName;
        private final int gamesPlayed;
        private final int goals;
        private final int assists;
        private final int points;
        private final int penaltyMinutes;

        private final int wins;
        private final int losses;
        private final int ties;
        private final int shutouts;
        private final int goalsAgainst;
    }
}
