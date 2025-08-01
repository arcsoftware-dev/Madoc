package dev.arcsoftware.madoc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatsCategory {
    GAMES_PLAYED("GP"),
    GOALS("G"),
    ASSISTS("A"),
    POINTS("P"),
    POINTS_PER_GAME("PPG"),
    PENALTY_MINUTES("PM"),
    WINS("W"),
    LOSSES("L"),
    TIES("T"),
    SHUTOUTS("SO"),
    EMPTY_NET_GOALS("ENG"),
    GOALS_AGAINST("GA"),
    GOALS_AGAINST_AVERAGE("GAA");

    private final String code;

    @JsonCreator
    public static StatsCategory fromCode(String code) {
        for (StatsCategory category : StatsCategory.values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown stats category: " + code);
    }

}
