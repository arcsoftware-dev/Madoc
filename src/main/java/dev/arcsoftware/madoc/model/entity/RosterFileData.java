package dev.arcsoftware.madoc.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RosterFileData {
    private int id;
    private int year;
    private String fileName;
    private byte[] fileContent;
    private LocalDateTime uploadedAt;

    public RosterFileData(int year, String fileName, byte[] fileContent) {
        this.year = year;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }
}
