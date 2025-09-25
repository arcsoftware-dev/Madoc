package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.model.payload.ScheduleUploadResult;
import dev.arcsoftware.madoc.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("")
    public ResponseEntity<List<ScheduleItemDto>> getSchedule(
            @RequestParam(value = "year", required = true) Integer year,
            @RequestParam(value = "season-type", required = true) SeasonType seasonType
    ){
        log.info("Fetching schedule for year: {}, seasonType: {}", year, seasonType);
        List<ScheduleItemDto> schedule = scheduleService.getSchedule(seasonType, year);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleItemDto>> getUpcomingSchedule(
            @RequestParam(value = "limit", defaultValue = "3")  Integer limit
    ){
        log.info("Fetching upcoming schedule for the next {} games", limit);
        List<ScheduleItemDto> schedule = scheduleService.getUpcomingMatches(limit);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedScheduleDto>> getGroupedSchedule(
            @RequestParam(value = "year", required = true) Integer year,
            @RequestParam(value = "season-type", required = true) SeasonType seasonType
    ){
        log.info("Fetching grouped schedule for year: {}, seasonType: {}", year, seasonType);
        List<GroupedScheduleDto> groupedSchedule = scheduleService.getGroupedSchedule(seasonType, year);
        return ResponseEntity.ok(groupedSchedule);
    }

    @GetMapping(value = "/printable", produces = "application/csv")
    public ResponseEntity<Resource> printable(
            @RequestParam(value = "year", required = true) Integer year,
            @RequestParam(value = "season-type", required = true) SeasonType seasonType,
            @RequestParam(value = "team-filter", required = false) String teamFilter
    ) {
        log.info("Fetching printable schedule for year: {}, seasonType: {}, team-filter: {}",
                year,
                seasonType,
                teamFilter
        );

        byte[] csvBytes = scheduleService.generatePrintableScheduleCsv(year, seasonType, teamFilter);
        String team = teamFilter == null ? "All" : teamFilter;
        String fileName = String.format("MMHL-Schedule-%s-%d-%s.csv", team, year, seasonType.name().toLowerCase());
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csvBytes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(resource);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScheduleUploadResult> uploadSchedules(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") int year,
            @RequestParam("season-type") SeasonType seasonType
    ) throws IOException {
        log.info("Received schedule upload request for year: {}", year);
        byte[] fileBytes = file.getBytes();
        UploadFileData uploadFileData = new UploadFileData(year, file.getOriginalFilename(), fileBytes);

        ScheduleUploadResult scheduleUploadResult = scheduleService.uploadSchedule(uploadFileData, seasonType);

        return ResponseEntity.ok(scheduleUploadResult);
    }
}
