package dev.arcsoftware.madoc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SortOrder {
    ASC,
    DESC;

    @JsonCreator
    public static SortOrder fromName(String name) {
        for (SortOrder sortOrder : SortOrder.values()) {
            if (sortOrder.name().equalsIgnoreCase(name)) {
                return sortOrder;
            }
        }
        throw new IllegalArgumentException("Unknown sort order: " + name);
    }
}
