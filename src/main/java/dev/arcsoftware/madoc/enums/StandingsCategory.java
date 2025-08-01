package dev.arcsoftware.madoc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StandingsCategory {
    WINS("W"),
    LOSSES("L"),
    TIES("T"),
    POINTS("P"),
    GOALS_FOR("GF"),
    GOALS_AGAINST("GA"),
    PENALTY_MINUTES("PM");

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
