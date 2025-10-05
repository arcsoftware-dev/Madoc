package dev.arcsoftware.madoc.auth.model;

import dev.arcsoftware.madoc.enums.Role;

public record AddUserRequest(
        String username,
        String password,
        String confirmPassword,
        Role role
) {}
