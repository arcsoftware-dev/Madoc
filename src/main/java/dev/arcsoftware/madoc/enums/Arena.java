package dev.arcsoftware.madoc.enums;

import lombok.Getter;

@Getter
public enum Arena {
    CENTURY("Century Gardens", "340 Vodden St E, Brampton, ON L6V 2N2");

    private final String arenaName;
    private final String address;

    Arena(String arenaName, String address) {
        this.arenaName = arenaName;
        this.address = address;
    }
}
