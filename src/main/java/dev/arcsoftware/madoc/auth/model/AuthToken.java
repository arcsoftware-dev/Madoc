package dev.arcsoftware.madoc.auth.model;

public record AuthToken(
        String validityPath,
        int expiryInSeconds,
        String jwt
) {
    public AuthToken(int expiryInSeconds, String jwt) {
        this("/", expiryInSeconds, jwt);
    }
}
