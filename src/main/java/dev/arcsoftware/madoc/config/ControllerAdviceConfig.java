package dev.arcsoftware.madoc.config;

import dev.arcsoftware.madoc.exception.ApiError;
import dev.arcsoftware.madoc.exception.ResultNotFoundException;
import dev.arcsoftware.madoc.exception.UnauthorizedException;
import dev.arcsoftware.madoc.model.ErrorSeverity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ControllerAdviceConfig {

    @ExceptionHandler(ResultNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResultNotFoundException(HttpServletRequest request, Model model, ResultNotFoundException ex) {
        log.error("", ex);
        model.addAttribute("path", request.getServletPath());
        model.addAttribute("error", "Not Found");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error";
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("", ex);
        return createResponse(HttpStatus.UNAUTHORIZED, ErrorSeverity.CRITICAL, ex.getMessage());
    }

    private ResponseEntity<ApiError> createResponse(HttpStatus status, ErrorSeverity severity, String message) {
        return new ResponseEntity<>(new ApiError(severity, message, status.value()), status);
    }
}
