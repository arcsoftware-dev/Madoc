package dev.arcsoftware.madoc.enums;

public enum Period {
    P1("1"),
    P2("2"),
    P3("3"),
    OT("OT"),
    SO("SO");

    private final String periodId;

    Period(String periodId) {
        this.periodId = periodId;
    }

    public static Period fromPeriodId(String periodId) {
        for (Period period : values()) {
            if (period.periodId.equals(periodId)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid period ID: " + periodId);
    }
}
