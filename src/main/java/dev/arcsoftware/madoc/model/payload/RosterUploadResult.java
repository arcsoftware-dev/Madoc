package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RosterUploadResult {
    private List<RosterAssignment> rosterAssignments;
    private int rosterFileId;
    private int year;
    private String fileName;
}
