package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.exception.UnauthorizedException;
import dev.arcsoftware.madoc.model.entity.GameUploadData;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.*;
import dev.arcsoftware.madoc.service.GameService;
import dev.arcsoftware.madoc.service.RosterService;
import dev.arcsoftware.madoc.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RosterService rosterService;
    private final ScheduleService scheduleService;
    private final GameService gameService;

    @Autowired
    public AdminController(RosterService rosterService, ScheduleService scheduleService, GameService gameService) {
        this.rosterService = rosterService;
        this.scheduleService = scheduleService;
        this.gameService = gameService;
    }

    @Value("${admin.token}")
    private String systemAdminToken;

    @PostMapping(value = "/upload/roster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RosterUploadResult> uploadRosters(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") int year
    ) throws IOException {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received roster upload request for year: {}", year);
        byte[] fileBytes = file.getBytes();
        UploadFileData uploadFileData = new UploadFileData(year, file.getOriginalFilename(), fileBytes);

        RosterUploadResult rosterUploadResult = rosterService.assignRosters(uploadFileData);

        return ResponseEntity.ok(rosterUploadResult);
    }

    @PostMapping(value = "/upload/schedule", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScheduleUploadResult> uploadSchedules(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") int year,
            @RequestParam("season-type") SeasonType seasonType
    ) throws IOException {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received schedule upload request for year: {}", year);
        byte[] fileBytes = file.getBytes();
        UploadFileData uploadFileData = new UploadFileData(year, file.getOriginalFilename(), fileBytes);

        ScheduleUploadResult scheduleUploadResult = scheduleService.uploadSchedule(uploadFileData, seasonType);

        return ResponseEntity.ok(scheduleUploadResult);
    }

    @PostMapping(value = "/upload/gamesheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GamesheetSummary> uploadGamesheet(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam("file") MultipartFile gamesheetFile
    ) throws IOException {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received gamesheet upload request");
        byte[] gamesheetFileBytes = gamesheetFile.getBytes();
        GameUploadData gameUploadData = new GameUploadData(gamesheetFile.getOriginalFilename(), gamesheetFileBytes);

        GamesheetSummary summary = gameService.uploadGamesheet(gameUploadData);

        return ResponseEntity.ok(summary);
    }

    @PostMapping(value = "/upload/attendance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceUploadResult> uploadAttendance(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received attendance upload request");
        byte[] fileBytes = file.getBytes();
        GameUploadData gameUploadData = new GameUploadData(file.getOriginalFilename(), fileBytes);

        AttendanceUploadResult attendanceUploadResult = gameService.uploadAttendance(gameUploadData);

        return ResponseEntity.ok(attendanceUploadResult);
    }

    @PutMapping(value = "/rosterAssignments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RosterAssignmentDto>> modifyRosterAssignments(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestBody List<RosterAssignmentDto> rosterAssignments
    ) {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received modify roster assignments request");
        rosterService.modifyRosters(rosterAssignments);

        return ResponseEntity.ok(rosterAssignments);
    }

    @GetMapping(value = "/rosterAssignments")
    public ResponseEntity<List<RosterAssignmentDto>> getRosterAssignmentsByTeam(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam("year") Integer year,
            @RequestParam("team_name") String teamName
    ) {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received get roster assignments request for year {}, team {}", year, teamName);
        List<RosterAssignmentDto> assignments = rosterService.getAssignedRostersByYearAndTeam(year, teamName);

        return ResponseEntity.ok(assignments);
    }

    private boolean isValidAdminToken(String token) {
        // Placeholder for actual token validation logic
        if(!this.systemAdminToken.equals(token)){
            log.error("Invalid admin token");
            return false;
        }
        else{
            log.info("Admin token validated successfully");
            return true;
        }
    }
}
