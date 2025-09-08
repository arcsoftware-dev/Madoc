package dev.arcsoftware.madoc.exception;

import dev.arcsoftware.madoc.model.ErrorSeverity;

public record ApiError (
    ErrorSeverity severity,
    String message,
    int status
) {}

