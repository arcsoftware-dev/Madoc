package dev.arcsoftware.madoc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StandingsCategory {
    GAMES_PLAYED("GP"),
    WINS("W"),
    LOSSES("L"),
    TIES("T"),
    POINTS("P"),
    GOALS_FOR("GF"),
    GOALS_AGAINST("GA"),
    PENALTY_MINUTES("PIM"),
    POINT_PERCENTAGE("PPCT");

    private final String code;

    @JsonCreator
    public static StandingsCategory fromCode(String code) {
        for (StandingsCategory category : StandingsCategory.values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown standings category: " + code);
    }
}
