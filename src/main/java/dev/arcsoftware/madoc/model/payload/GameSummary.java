package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.SeasonType;

import java.time.LocalDateTime;

public record GameSummary(
        String id,
        LocalDateTime gameTime,
        int year,
        SeasonType seasonType,
        String homeTeam,
        int homeTeamScore,
        int homeTeamPenaltyMinutes,
        String awayTeam,
        int awayTeamScore,
        int awayTeamPenaltyMinutes
) {}
