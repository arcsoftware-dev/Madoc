package dev.arcsoftware.madoc.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamEntity {
    private Integer id;
    private String teamName;
    private int year;

    public Map<String, Object> toParameterMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("team_name", teamName);
        map.put("year", year);
        return map;
    }
}
