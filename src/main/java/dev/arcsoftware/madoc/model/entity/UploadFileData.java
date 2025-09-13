package dev.arcsoftware.madoc.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class UploadFileData {
    private int id;
    private int year;
    private String fileName;
    private byte[] fileContent;
    private LocalDateTime uploadedAt;

    public UploadFileData(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public UploadFileData(int year, String fileName, byte[] fileContent) {
        this.year = year;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public Map<String, Object> toParameterMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("year", year);
        map.put("file_name", fileName);
        map.put("file_content", fileContent);
        map.put("uploaded_at", uploadedAt);
        return map;
    }
}
