package dev.arcsoftware.madoc.enums;

import lombok.Getter;

public enum Position {
    SKATER("S", "Skater"),
    FORWARD("F", "Forward"),
    DEFENCE("D", "Defence"),
    GOALIE("G", "Goalie");

    @Getter
    private final String code;
    @Getter
    private final String label;

    Position(String code, String label) {
        this.code = code;
        this.label = label;
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
