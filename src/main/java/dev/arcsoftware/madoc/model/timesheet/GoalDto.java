package dev.arcsoftware.madoc.model.timesheet;

import dev.arcsoftware.madoc.enums.Period;
import lombok.Data;

@Data
public class GoalDto {
    private Period period;
    private String time;
    private String teamName;
    private int goalScorerJerseyNumber;
    private int primaryAssistJerseyNumber;
    private int secondaryAssistJerseyNumber;
}
