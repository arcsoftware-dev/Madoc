package dev.arcsoftware.madoc.model.payload;

import lombok.Data;

@Data
public class NextPenaltyPayload {
    private String team;
    private PenaltyPayload penalty;
}
