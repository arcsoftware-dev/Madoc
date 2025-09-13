package dev.arcsoftware.madoc.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.Map;

@Data
public class AssistEntity {
    private Integer id;
    private PlayerEntity player;
    private boolean isPrimary;
    @Transient
    private int jerseyNumber;

    public AssistEntity(PlayerEntity player, boolean isPrimary) {
        this.player = player;
        this.isPrimary = isPrimary;
    }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("player_id", player.getId());
        map.put("is_primary", isPrimary);
        return map;
    }
}