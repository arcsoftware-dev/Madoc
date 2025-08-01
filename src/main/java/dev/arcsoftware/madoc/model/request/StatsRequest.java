package dev.arcsoftware.madoc.model.request;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;

public record StatsRequest(
        PlayerType playerType,
        Integer year,
        SeasonType seasonType,
        StatsCategory sortCategory,
        SortOrder sortOrder
) {}
