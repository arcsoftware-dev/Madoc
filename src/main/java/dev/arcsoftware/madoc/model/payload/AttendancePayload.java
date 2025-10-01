package dev.arcsoftware.madoc.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class AttendancePayload {
    private String playerName;
    private Integer playerId;
    private Integer teamId;
    private Integer gameId;
    private Integer jerseyNumber;
    private Boolean attended;
}