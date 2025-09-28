package dev.arcsoftware.madoc.auth.model;

public record AuthenticationRequest(
        String username,
        String password
) {}
