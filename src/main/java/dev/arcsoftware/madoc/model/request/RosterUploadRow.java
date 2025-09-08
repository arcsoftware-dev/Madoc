package dev.arcsoftware.madoc.model.request;

import dev.arcsoftware.madoc.enums.DraftRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RosterUploadRow {
    private int number;
    private String player;
    private String team;
    private DraftRank draftRank;
    private boolean isRookie;
}
