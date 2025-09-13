package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import lombok.Getter;
import lombok.Setter;

public class RosterAssignmentDto extends RosterAssignment {
    @Getter
    @Setter
    private String playerName;
}
