package dev.arcsoftware.madoc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeasonType {
    REGULAR_SEASON("R", "Regular Season"),
    PLAYOFFS("P", "Playoffs");

    private final String code;
    private final String label;

    public static SeasonType fromCode(String code){
        for (SeasonType seasonType : SeasonType.values()) {
            if (seasonType.code.equalsIgnoreCase(code)){
                return seasonType;
            }
        }
        throw new IllegalArgumentException("Unknown season type: " + code);
    }
}
