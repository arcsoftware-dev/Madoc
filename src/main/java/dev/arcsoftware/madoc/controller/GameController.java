package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.entity.GameUploadData;
import dev.arcsoftware.madoc.model.payload.AttendanceDto;
import dev.arcsoftware.madoc.model.payload.AttendanceUploadResult;
import dev.arcsoftware.madoc.model.payload.GamesheetSummary;
import dev.arcsoftware.madoc.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @PostMapping(value = "/upload/gamesheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GamesheetSummary> uploadGamesheet(
            @RequestParam("file") MultipartFile gamesheetFile
    ) throws IOException {
        log.info("Received gamesheet upload request");
        byte[] gamesheetFileBytes = gamesheetFile.getBytes();
        GameUploadData gameUploadData = new GameUploadData(gamesheetFile.getOriginalFilename(), gamesheetFileBytes);

        GamesheetSummary summary = gameService.uploadGamesheet(gameUploadData);

        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @PostMapping(value = "/upload/attendance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceUploadResult> uploadAttendance(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("Received attendance upload request");
        byte[] fileBytes = file.getBytes();
        GameUploadData gameUploadData = new GameUploadData(file.getOriginalFilename(), fileBytes);

        AttendanceUploadResult attendanceUploadResult = gameService.uploadAttendance(gameUploadData);

        return ResponseEntity.ok(attendanceUploadResult);
    }

    @PreAuthorize("hasAnyRole('ROLE_[ADMIN]', 'ROLE_[LEAGUE_STAFF]', 'ROLE_[TIMEKEEPER]')")
    @PostMapping(value = "/attendance", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttendanceUploadResult> addAttendanceReport(
            @RequestBody AttendanceDto attendance
    ) {
        log.info("Received attendance report");

        AttendanceUploadResult attendanceReport = gameService.addAttendanceReport(attendance);

        return ResponseEntity.ok(attendanceReport);
    }
}
