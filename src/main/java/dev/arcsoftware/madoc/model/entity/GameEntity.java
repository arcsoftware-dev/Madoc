package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class GameEntity {
    private Integer id;
    private TeamEntity homeTeam;
    private TeamEntity awayTeam;
    private int year;
    private SeasonType seasonType;
    private Arena venue;
    private LocalDateTime gameTime;

    private String refereeNameOne;
    private String refereeNameTwo;
    private String refereeNameThree;
    private List<String> refereeNotes;

    private boolean isFinalized;
    private LocalDateTime finalizedAt;

    public GameEntity(int id) {
        this.id = id;
    }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("home_team", homeTeam.getId());
        map.put("away_team", awayTeam.getId());
        map.put("year", year);
        map.put("season_type", seasonType.name());
        map.put("venue", venue.name());
        map.put("game_time", gameTime);
        map.put("referee_name_one", refereeNameOne);
        map.put("referee_name_two", refereeNameTwo);
        map.put("referee_name_three", refereeNameThree);
        map.put("referee_notes", refereeNotes);
        map.put("is_finalized", isFinalized);
        map.put("finalized_at", finalizedAt);
        return map;
    }
}
