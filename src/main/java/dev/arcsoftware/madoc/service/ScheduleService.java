package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<ScheduleItemDto> getUpcomingMatches() {
        return scheduleRepository.getUpcomingMatches();
    }
}
