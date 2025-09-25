package dev.arcsoftware.madoc.model.request;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;

import static dev.arcsoftware.madoc.enums.StatsCategory.POINTS;

public record StatsRequest(
        PlayerType playerType,
        Integer year,
        SeasonType seasonType,
        StatsCategory sortCategory,
        SortOrder sortOrder
) {
    public StatsRequest(PlayerType playerType, Integer year, SeasonType seasonType, StatsCategory sortCategory, SortOrder sortOrder) {
        this.playerType = playerType;
        this.year = year;
        this.seasonType = seasonType;
        this.sortCategory = sortCategory == null
                ? (PlayerType.GOALIES.equals(playerType)
                    ? StatsCategory.GOALS_AGAINST_AVERAGE
                    : POINTS)
                : sortCategory;

        this.sortOrder = sortOrder == null
                        ? (PlayerType.GOALIES.equals(playerType)
                            ? SortOrder.ASC
                            : SortOrder.DESC)
                        : sortOrder;
    }
}
