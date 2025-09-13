package dev.arcsoftware.madoc.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Data
public class AttendanceEntity {
    private Integer id;
    private GameEntity game;
    private PlayerEntity player;
    private int jerseyNumber;
    private TeamEntity team;
    private boolean attended;

    public Map<String, Object> toParameterMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("game_id", game.getId());
        map.put("player_id", player.getId());
        map.put("jersey_number", jerseyNumber);
        map.put("team_id", team.getId());
        map.put("attended", attended);
        return map;
    }
}
