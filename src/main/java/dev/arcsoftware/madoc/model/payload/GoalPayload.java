package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.GoalType;
import lombok.Data;


@Data
public class GoalPayload {
    private Integer scorer;
    private Integer assist1;
    private Integer assist2;
    private Integer period;
    private String time;
    private GoalType goalType;

    public void setGoalTypeFromCode(String code) {
        if(code == null || "".equals(code)) {
            this.goalType = GoalType.REGULAR;
        }
        else{
            this.goalType = GoalType.fromCode(code.toUpperCase());
        }
    }
}