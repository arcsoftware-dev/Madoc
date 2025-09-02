package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.timesheet.RosterDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class RosterRepository {

    private static List<RosterDto> static2024Rosters;

    @PostConstruct
    public void loadData(){
        static2024Rosters = new ArrayList<>();
        ClassPathResource rosterResource = new ClassPathResource("data/rosters/2024.csv");
        try(BufferedReader rosters = new BufferedReader(new BufferedReader(new InputStreamReader(rosterResource.getInputStream())))) {
            rosters.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //#,Player,Team,Draft Rank,isRookie
                        String[] split = line.split(",");

                        DraftRank draftRank = DraftRank.valueOf(split[3]);
                        Position position = Position.fromCode(draftRank.name().substring(0, 1));
                        boolean rookieStatus = split[4].equalsIgnoreCase("true");
                        RosterDto rosterDto = RosterDto.builder()
                                .jerseyNumber(Integer.parseInt(split[0]))
                                .teamName(split[2])
                                .fullName(split[1])
                                .firstName(split[1].split(" ")[0])
                                .lastName(split[1].split(" ")[1])
                                .position(position)
                                .draftRank(draftRank)
                                .isRookie(rookieStatus)
                                .build();
                        static2024Rosters.add(rosterDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RosterDto> getRosters() {
        return static2024Rosters;
    }
}
