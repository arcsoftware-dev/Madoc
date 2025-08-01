package dev.arcsoftware.madoc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlayerType {
    SKATERS("S"),
    ROOKIES("R"),
    FORWARDS("F"),
    DEFENCE("D"),
    GOALIES("G");

    private final String code;

    @JsonCreator
    public static PlayerType fromCode(String code) {
        for (PlayerType playerType : PlayerType.values()) {
            if (playerType.code.equalsIgnoreCase(code)) {
                return playerType;
            }
        }
        throw new IllegalArgumentException("Unknown stats type: " + code);
    }
}
