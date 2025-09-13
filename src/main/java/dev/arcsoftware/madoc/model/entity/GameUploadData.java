package dev.arcsoftware.madoc.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GameUploadData extends UploadFileData {
    private int gameId;

    public GameUploadData(String fileName, byte[] fileContent) {
        super(fileName, fileContent);
    }

    @Override
    public Map<String, Object> toParameterMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.getId());
        map.put("game_id", this.getGameId());
        map.put("file_name", this.getFileName());
        map.put("file_content", this.getFileContent());
        map.put("uploaded_at", this.getUploadedAt());
        return map;
    }
}
