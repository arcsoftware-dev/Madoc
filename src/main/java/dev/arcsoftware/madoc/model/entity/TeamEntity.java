package dev.arcsoftware.madoc.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamEntity {
    private Integer id;
    private String teamName;
    private int year;
}
