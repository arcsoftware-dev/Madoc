package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.entity.GameEntity;

import java.util.List;

public record PregamePackageGameDto(
        GameEntity game,
        List<RosterRow> homeGamesheetRows,
        List<RosterRow> awayGamesheetRows,
        List<RosterRow> homeSignInRows,
        List<RosterRow> awaySignInRows
) {
    public record RosterRow(String jerseyNumber, String fullName) {
        public static final RosterRow BLANK = new RosterRow("", "");
    }
}
