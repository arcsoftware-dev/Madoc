package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.GoalType;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class GoalEntity {
    private Integer id;
    private GameEntity game;
    private GoalType goalType;
    private RosterAssignment scorer;
    private RosterAssignment primaryAssistPlayer;
    private RosterAssignment secondaryAssistPlayer;
    private int period;
    private LocalTime time;
    private LocalDateTime uploadedAt;

    @Transient
    private Integer jerseyNumber;
    @Transient
    private Integer primaryAssistJerseyNumber;
    @Transient
    private Integer secondaryAssistJerseyNumber;

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("game_id", game.getId());
        map.put("goal_type", goalType.name());
        map.put("scorer_id", scorer.getId());
        map.put("primary_assist_id", primaryAssistPlayer != null ? primaryAssistPlayer.getId() : null);
        map.put("secondary_assist_id", secondaryAssistPlayer != null ? secondaryAssistPlayer.getId() : null);
        map.put("period", period);
        map.put("time", time.toString());
        map.put("uploaded_at", uploadedAt);
        return map;
    }
}