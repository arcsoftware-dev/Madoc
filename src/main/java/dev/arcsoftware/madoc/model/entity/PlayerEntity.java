package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;

public class PlayerEntity {

    private Integer playerId;

    private Integer memberId;

    private Position position;

    private Integer jerseyNumber;

    private DraftRank draftRank;

    private boolean isRookie;

    private Integer year;
}
