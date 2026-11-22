package dev.arcsoftware.madoc.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PresidentEntity {

    private static final Set<String> NAME_SUFFIXES = Set.of("sr", "sr.", "jr", "jr.", "ii", "iii", "iv");

    private int number;
    private String name;
    private int startYear;
    private Integer endYear;
    private String photoId;

    public boolean isCurrent() {
        return endYear == null;
    }

    public boolean hasPhoto() {
        return photoId != null && !photoId.isBlank();
    }

    public boolean hasExternalPhotoUrl() {
        return hasPhoto() && (photoId.startsWith("http://") || photoId.startsWith("https://"));
    }

    public String getTermDisplay() {
        return endYear == null ? startYear + " – Present" : startYear + " – " + endYear;
    }

    public int getYearsServed() {
        int end = endYear != null ? endYear : Year.now().getValue();
        return Math.max(0, end - startYear);
    }

    public String getYearsServedDisplay() {
        int years = getYearsServed();
        if (years <= 0) {
            return "< 1 yr";
        }
        return years == 1 ? "1 yr" : years + " yrs";
    }

    public String getOrdinal() {
        int mod100 = number % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return number + "th";
        }
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }

    public String getInitials() {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = null;
        String last = null;
        for (String part : parts) {
            if (NAME_SUFFIXES.contains(part.toLowerCase())) {
                continue;
            }
            if (first == null) {
                first = part;
            } else {
                last = part;
            }
        }
        if (first == null) {
            return "?";
        }
        if (last == null) {
            return first.substring(0, 1).toUpperCase();
        }
        return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
    }
}
