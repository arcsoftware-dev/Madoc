package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlayerGameLogDto {
    private final int gameId;
    private final LocalDateTime gameTime;
    private final int seasonYear;
    private final SeasonType seasonType;
    private final boolean home;
    private final Position position;
    private final String teamName;
    private final String opponentTeamName;
    private final int teamScore;
    private final int opponentScore;
    private final int goals;
    private final int assists;
    private final int points;
    private final int penaltyMinutes;
    private final String result;
    private final boolean shutout;
}
