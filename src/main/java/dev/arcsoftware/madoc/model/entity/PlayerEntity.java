package dev.arcsoftware.madoc.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class PlayerEntity {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public PlayerEntity(int id){
        this.id = id;
    }

    public Map<String, Object> toParameterMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("first_name", firstName);
        map.put("last_name", lastName);
        map.put("email", email);
        map.put("phone_number", phoneNumber);
        map.put("created_at", createdAt);
        return map;
    }
}
