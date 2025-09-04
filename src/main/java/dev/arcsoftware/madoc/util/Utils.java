package dev.arcsoftware.madoc.util;

import org.springframework.stereotype.Component;

@Component
public class Utils {

    public String abbreviateTeamName(String teamName) {
        return switch (teamName.toUpperCase()){
            case "VEGAS" -> "VGK";
            case "BLACKHAWKS" -> "BLK";
            case "WHALERS" -> "WHL";
            case "RED WINGS" -> "RDW";
            case "LEAFS" -> "LFS";
            case "AVALANCHE" -> "AVL";
            default -> teamName.length() <= 3 ? teamName : teamName.substring(0, 3).toUpperCase();
        };
    }

}
