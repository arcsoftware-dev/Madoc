package dev.arcsoftware.madoc.model.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlayerGameLogPage {
    private final List<PlayerGameLogDto> games;
    private final int totalGames;
    private final int offset;
    private final int limit;

    public boolean hasMore() {
        return offset + games.size() < totalGames;
    }

    public int getRemaining() {
        return Math.max(0, totalGames - (offset + games.size()));
    }
}
