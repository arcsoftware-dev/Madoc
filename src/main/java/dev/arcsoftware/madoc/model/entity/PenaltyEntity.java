package dev.arcsoftware.madoc.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class PenaltyEntity {
    private int id;
    private GameEntity game;
    private PlayerEntity player;
    private String infraction;
    private int minutes;
    private int period;
    private LocalTime time;
    private LocalDateTime uploadedAt;

    @Transient
    private int jerseyNumber;

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("game_id", game.getId());
        map.put("player_id", player.getId());
        map.put("infraction", infraction);
        map.put("minutes", minutes);
        map.put("period", period);
        map.put("time", time.toString());
        map.put("uploaded_at", uploadedAt);
        return map;
    }
}
