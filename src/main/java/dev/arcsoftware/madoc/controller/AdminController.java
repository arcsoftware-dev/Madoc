package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.exception.UnauthorizedException;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.RosterFileData;
import dev.arcsoftware.madoc.model.payload.RosterUploadResult;
import dev.arcsoftware.madoc.service.RosterService;
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

    @Autowired
    public AdminController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @Value("${admin.token}")
    private String adminToken;

    @PostMapping(value = "/rosters/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RosterUploadResult> uploadRosters(
            @RequestParam("X-Admin-Token") String adminToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") int year
    ) throws IOException {
        if(!isValidAdminToken(adminToken)){
            throw new UnauthorizedException("Invalid admin token");
        }

        log.info("Received roster upload request for year: {}", year);
        byte[] fileBytes = file.getBytes();

        RosterFileData rosterFileData = new RosterFileData(year, file.getOriginalFilename(), fileBytes);

        List<RosterAssignment> rosterAssignments = rosterService.assignRosters(rosterFileData);

        return ResponseEntity.ok()
                .body(
                        new RosterUploadResult(
                            rosterAssignments,
                            rosterFileData.getId(),
                            rosterFileData.getYear(),
                            rosterFileData.getFileName()
                        )
                );
    }

    private boolean isValidAdminToken(String token) {
        // Placeholder for actual token validation logic
        if(!this.adminToken.equals(token)){
            log.error("Invalid admin token");
            return false;
        }
        else{
            log.info("Admin token validated successfully");
            return true;
        }
    }
}
