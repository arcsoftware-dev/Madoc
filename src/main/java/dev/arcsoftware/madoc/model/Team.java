package dev.arcsoftware.madoc.model;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class Team {
    private Integer teamId;
    private File logoResource;
    private String teamName;
    private List<Player> roster;
    private TeamStats teamStats;
    private List<GameSheet> schedule;
}
