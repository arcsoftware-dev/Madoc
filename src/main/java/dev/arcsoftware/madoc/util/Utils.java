package dev.arcsoftware.madoc.util;

import org.springframework.stereotype.Component;

@Component
public class Utils {

    private static char[] delimiters = new char[]{' ', '\''};

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

    public static String toCamelCase(String original){
        if(original == null || original.isBlank()) return original;
        char[] chars = original.toCharArray();

        StringBuilder sb = new StringBuilder();

        boolean nextIsUpper = true;
        for(char c : chars){
            boolean isDelimiter = false;
            for(char d : delimiters){
                if(c==d){
                    isDelimiter = true;
                    break;
                }
            }
            if(isDelimiter){
                sb.append(c);
                nextIsUpper = true;
            }
            else{
                sb.append(nextIsUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextIsUpper = false;
            }

        }

        return sb.toString().trim();
    }

}
