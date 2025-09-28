package dev.arcsoftware.madoc.auth.model;

public record AuthenticationResponse (
        boolean success,
        String message
) {}
