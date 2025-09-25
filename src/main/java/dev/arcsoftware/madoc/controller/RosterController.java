package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.RosterUploadResult;
import dev.arcsoftware.madoc.service.RosterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/roster")
public class RosterController {

    private final RosterService rosterService;

    @Autowired
    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RosterUploadResult> uploadRosters(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") int year
    ) throws IOException {
        log.info("Received roster upload request for year: {}", year);
        byte[] fileBytes = file.getBytes();
        UploadFileData uploadFileData = new UploadFileData(year, file.getOriginalFilename(), fileBytes);

        RosterUploadResult rosterUploadResult = rosterService.assignRosters(uploadFileData);

        return ResponseEntity.ok(rosterUploadResult);
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RosterAssignmentDto>> modifyRosterAssignments(
            @RequestBody List<RosterAssignmentDto> rosterAssignments
    ) {
        log.info("Received modify roster assignments request");
        rosterService.modifyRosters(rosterAssignments);

        return ResponseEntity.ok(rosterAssignments);
    }

    @GetMapping(value = "/{year}/{team-name}")
    public ResponseEntity<List<RosterAssignmentDto>> getRosterAssignmentsByTeam(
            @PathVariable("year") Integer year,
            @PathVariable("team-name") String teamName
    ) {
        log.info("Received get roster assignments request for year {}, team {}", year, teamName);
        List<RosterAssignmentDto> assignments = rosterService.getAssignedRostersByYearAndTeam(year, teamName);

        return ResponseEntity.ok(assignments);
    }

    @GetMapping(value = "/{year}")
    public ResponseEntity<List<List<RosterAssignmentDto>>> getRostersByYear(
            @PathVariable("year") Integer year
    ) {
        log.info("Received get rosters request for year {}, all teams", year);
        List<List<RosterAssignmentDto>> assignments = rosterService.getAssignedRostersByYear(year);

        return ResponseEntity.ok(assignments);
    }
}
