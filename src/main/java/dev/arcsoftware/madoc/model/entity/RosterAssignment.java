package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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
    private boolean isActive;

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("player_id", playerId);
        map.put("team_id", teamId);
        map.put("season_year", seasonYear);
        map.put("draft_position", draftPosition.name());
        map.put("position", position.name());
        map.put("jersey_number", jerseyNumber);
        map.put("is_rookie", isRookie);
        map.put("is_active", isActive);
        return map;
    }
}
