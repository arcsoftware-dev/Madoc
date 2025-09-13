package dev.arcsoftware.madoc.enums;

public enum GoalType {
    REGULAR(""),
    POWER_PLAY("PP"),
    PENALTY_SHOT("PS"),
    SHORT_HANDED("SH"),
    EMPTY_NET("EN");

    private String code;

    GoalType(String code) {
        this.code = code;
    }

    public static GoalType fromCode(String code) {
        for (GoalType type : GoalType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GoalType From Code: " + code);
    }
}
