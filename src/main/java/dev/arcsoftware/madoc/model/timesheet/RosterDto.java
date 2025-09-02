package dev.arcsoftware.madoc.model.timesheet;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RosterDto {
    private int jerseyNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private Position position;
    private DraftRank draftRank;
    private boolean isRookie;
    private String teamName;
}
