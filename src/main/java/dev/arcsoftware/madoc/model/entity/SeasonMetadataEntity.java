package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.SeasonType;

public record SeasonMetadataEntity(SeasonType seasonType, Integer seasonYear) {}
