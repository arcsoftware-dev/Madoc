package dev.arcsoftware.madoc.model.timesheet;

import dev.arcsoftware.madoc.enums.Period;
import lombok.Data;

@Data
public class PenaltyDto {
    private Period period;
    private String teamName;
    private String timeOfPenalty;
    private int offenderJerseyNumber;
    private String offence;
    private int duration; // in minutes
}
