package dev.arcsoftware.madoc.auth.model;

public record ChangePasswordRequest(
        String username,
        String oldPassword,
        String newPassword
) {}
