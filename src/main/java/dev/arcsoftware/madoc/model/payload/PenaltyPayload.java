package dev.arcsoftware.madoc.model.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PenaltyPayload {
    private Integer jerseyNumber;
    private String infraction;
    private Integer minutes;
    private Integer period;
    private String time;
}
