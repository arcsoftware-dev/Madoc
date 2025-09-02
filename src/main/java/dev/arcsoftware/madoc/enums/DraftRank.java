package dev.arcsoftware.madoc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DraftRank {
    F_A1(1),
    F_A2(2),
    F_A3(3),
    F_B1(4),
    F_B2(5),
    F_B3(6),
    F_C1(7),
    F_C2(8),
    F_C3(9),
    D_A1(10),
    D_A2(11),
    D_B1(12),
    D_B2(13),
    D_C1(14),
    D_C2(15),
    G(16);

    private final int rank;
}
