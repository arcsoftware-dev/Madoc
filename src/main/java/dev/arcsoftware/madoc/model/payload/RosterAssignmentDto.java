package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RosterAssignmentDto extends RosterAssignment {
    private String firstName;
    private String lastName;
    private String fullName;
    private String teamName;
}
