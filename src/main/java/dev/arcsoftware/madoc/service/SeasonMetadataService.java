package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.SeasonMetadataEntity;
import dev.arcsoftware.madoc.repository.SeasonMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeasonMetadataService {

    private final SeasonMetadataRepository seasonMetadataRepository;
    private SeasonMetadataEntity currentSeasonMetadataEntity;

    @Autowired
    public SeasonMetadataService(SeasonMetadataRepository seasonMetadataRepository) {
        this.seasonMetadataRepository = seasonMetadataRepository;
    }

    public SeasonType getCurrentSeasonType(){
        initMetadata();
        return this.currentSeasonMetadataEntity.seasonType();
    }

    public Integer getCurrentSeasonYear(){
        initMetadata();
        return this.currentSeasonMetadataEntity.seasonYear();
    }

    private void initMetadata() {
        if (currentSeasonMetadataEntity == null) {
            currentSeasonMetadataEntity = seasonMetadataRepository.getCurrentSeasonMetadata();
        }
    }
}
