package dev.arcsoftware.madoc.enums;

import lombok.Getter;

@Getter
public enum Arena {
    CENTURY("Century Gardens");

    private final String arenaName;

    Arena(String arenaName) {
        this.arenaName = arenaName;
    }
}
