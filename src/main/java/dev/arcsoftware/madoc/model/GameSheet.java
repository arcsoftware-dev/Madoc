package dev.arcsoftware.madoc.model;

import java.util.List;

public class GameSheet {
    private Integer gameSheetId;
    private Game game;
    private List<Goal> homeGoals;
    private List<Goal> awayGoals;
    private List<Penalty> homePenalties;
    private List<Penalty> awayPenalties;
}
