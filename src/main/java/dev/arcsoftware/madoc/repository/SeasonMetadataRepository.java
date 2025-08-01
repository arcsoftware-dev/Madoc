package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.SeasonMetadataEntity;
import org.springframework.stereotype.Repository;

@Repository
public class SeasonMetadataRepository {
    public SeasonMetadataEntity getCurrentSeasonMetadata() {
        // This method should return the current season metadata.
        // For now, we will return a hardcoded value.
        return new SeasonMetadataEntity(SeasonType.REGULAR_SEASON, 2025);
    }
}
