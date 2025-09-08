package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RosterAssignment {
    private Integer id;
    private Integer playerId;
    private Integer teamId;
    private Integer seasonYear;
    private DraftRank draftPosition;
    private Position position;
    private Integer jerseyNumber;
    private boolean isRookie;
}
