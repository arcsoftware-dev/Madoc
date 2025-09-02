package dev.arcsoftware.madoc.enums;

import lombok.Getter;

public enum Position {
    SKATER("S"),
    FORWARD("F"),
    DEFENCE("D"),
    GOALIE("G");

    @Getter
    private final String code;

    Position(String code) {
        this.code = code;
    }

    public static Position fromCode(String code) {
        for (Position position : Position.values()) {
            if (position.code.equalsIgnoreCase(code)) {
                return position;
            }
        }
        throw new IllegalArgumentException("Unknown position code: " + code);
    }
}
