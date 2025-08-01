package dev.arcsoftware.madoc.model.entity;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class TeamEntity {
    private Integer teamId;
    private File logoResource;
    private String teamName;
    private List<PlayerEntity> roster;

    private List<GameEntity> regularSeasonSchedule;
    private List<GameEntity> playoffSchedule;

    private Integer year;
}
