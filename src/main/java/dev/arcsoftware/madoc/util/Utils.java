package dev.arcsoftware.madoc.util;

import org.springframework.stereotype.Component;

@Component
public class Utils {

    public String abbreviateTeamName(String teamName) {
        return switch (teamName.toUpperCase()){
            case "VEGAS", "GOLDEN KNIGHTS" -> "VGK";
            case "BLACKHAWKS" -> "BLK";
            case "WHALERS" -> "WHL";
            case "RED WINGS" -> "RDW";
            case "LEAFS" -> "LFS";
            case "AVALANCHE" -> "AVL";
            case "CANUCKS" -> "CNK";
            default -> teamName.length() <= 3 ? teamName : teamName.substring(0, 3).toUpperCase();
        };
    }

    public String stripSpaces(String s){
        if(s == null) return "";
        return s.replace(" ", "");
    }

}
