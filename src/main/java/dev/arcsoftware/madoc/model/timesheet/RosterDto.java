package dev.arcsoftware.madoc.model.timesheet;

import dev.arcsoftware.madoc.enums.Position;
import lombok.Data;

@Data
public class RosterDto {
    private int jerseyNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private Position position;
    private String teamName;
}
