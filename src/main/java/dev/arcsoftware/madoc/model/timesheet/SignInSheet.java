package dev.arcsoftware.madoc.model.timesheet;

import lombok.Data;

import java.util.List;

@Data
public class SignInSheet {
    private int gameId;
    private String teamName;
    private RosterDto goalie;
    private List<RosterDto> players;
}
