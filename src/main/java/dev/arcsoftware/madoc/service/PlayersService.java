package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.repository.RosterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlayersService {

    private final RosterRepository rosterRepository;

    @Autowired
    public PlayersService(
            RosterRepository rosterRepository
    ) {
        this.rosterRepository = rosterRepository;
    }

    public List<PlayerEntity> getPlayers() {
        return rosterRepository.getAllPlayers();
    }

    public List<PlayerEntity> getAndCreatePlayersIfNotFound(Set<String> playerNames){
        //Look up player entities and add them if they don't exist
        List<PlayerEntity> playerEntities = getPlayers();
        int originalPlayerCount = playerEntities.size();
        log.info("Found {} players", originalPlayerCount);

        Set<String> existingPlayerEntities = playerEntities.stream()
                .map(p -> p.getFirstName() + " " + p.getLastName())
                .collect(Collectors.toSet());

        boolean hasNewPlayers = false;
        for(String playerName : playerNames){
            if(!existingPlayerEntities.contains(playerName)){
                hasNewPlayers = true;
                PlayerEntity playerEntity = new PlayerEntity();
                playerEntity.setFirstName(playerName.split(" ")[0]);
                playerEntity.setLastName(Arrays.stream(playerName.split(" ")).skip(1).collect(Collectors.joining(" ")));
                rosterRepository.insertPlayer(playerEntity);
            }
        }

        if(hasNewPlayers){
            playerEntities = getPlayers();
            log.info("Inserted an additional {} players", playerEntities.size() - originalPlayerCount);
        }
        return playerEntities;
    }
}
