package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.PresidentEntity;
import dev.arcsoftware.madoc.repository.PresidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PresidentService {

    private final PresidentRepository presidentRepository;

    @Autowired
    public PresidentService(PresidentRepository presidentRepository) {
        this.presidentRepository = presidentRepository;
    }

    public List<PresidentEntity> getAllPresidents() {
        return presidentRepository.getAllPresidents();
    }
}
