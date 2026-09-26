package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @GetMapping(value = "/signin/{id}/{team}")
    public ResponseEntity<byte[]> getSigninSheet(
            @PathVariable("id") Integer gameId,
            @PathVariable("team") String teamName
    ){
        log.info("Received sign in sheet get request with id={} for team={}", gameId, teamName);

        Pair<String, byte[]> exportData = documentService.getEmptySignInSheet(gameId, teamName);
        String downloadFileName = exportData.left()+".ods";
        String headerValue = "attachment; filename=\""+downloadFileName+"\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(exportData.right());
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @GetMapping(value = "/gamesheet/{id}")
    public ResponseEntity<byte[]> getGamesheet(
            @PathVariable("id") Integer gameId
    ){
        log.info("Received gamesheet get request with id={}", gameId);

        Pair<String, byte[]> exportData = documentService.getEmptyGamesheet(gameId);
        String downloadFileName = exportData.left()+".ods";
        String headerValue = "attachment; filename=\""+downloadFileName+"\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(exportData.right());
    }
}
