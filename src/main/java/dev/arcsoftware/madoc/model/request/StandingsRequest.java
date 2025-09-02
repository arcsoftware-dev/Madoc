package dev.arcsoftware.madoc.model.request;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;

public record StandingsRequest(
        Integer year,
        SeasonType seasonType,
        StandingsCategory sortCategory,
        SortOrder sortOrder
) {}
