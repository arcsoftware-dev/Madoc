package dev.arcsoftware.madoc.model;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.PlayerType;
import lombok.Data;

@Data
public class Player {
    private Integer playerId;
    private Team team;
    private PlayerType playerType;
    private String jersey;
    private DraftRank draftRank;
    private String firstName;
    private String lastName;
    private PlayerStats stats;
}
