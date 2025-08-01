package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.StatsDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Repository
public class StatsRepository {
    private static List<StatsDto> staticSkaterStats;
    private static List<StatsDto> staticGoalieStats;

    public List<StatsDto> getSkaterStats() {
        if(staticSkaterStats != null) {
            return staticSkaterStats;
        }
        else {
            staticSkaterStats = new ArrayList<>();
        }
        List<String> teamNames = List.of(
                "Leafs",
                "Avalanche",
                "Vegas",
                "Red Wings",
                "Blackhawks",
                "Whalers"
        );

        List<String> names = List.of(
                "Sidney Crosby", "Connor McDavid", "Auston Matthews", "Nathan MacKinnon", "Leon Draisaitl",
                "David Pastrnak", "Nikita Kucherov", "Alex Ovechkin", "Patrick Kane", "Jonathan Toews",
                "Steven Stamkos", "Jack Eichel", "Mitch Marner", "Brayden Point", "Artemi Panarin",
                "Mika Zibanejad", "Sebastian Aho", "Aleksander Barkov", "Mathew Barzal", "Mark Scheifele",
                "Blake Wheeler", "Patrice Bergeron", "Brad Marchand", "John Tavares", "William Nylander",
                "Gabriel Landeskog", "Mikko Rantanen", "Cale Makar", "Quinn Hughes", "Elias Pettersson",
                "Bo Horvat", "Brock Boeser", "Ryan O'Reilly", "Vladimir Tarasenko", "Roman Josi",
                "Dougie Hamilton", "Seth Jones", "Zach Werenski", "Johnny Gaudreau", "Matthew Tkachuk",
                "Sean Monahan", "Elias Lindholm", "Rasmus Dahlin", "Jack Hughes", "Nico Hischier",
                "Jesper Bratt", "Kyle Palmieri", "Anders Lee", "Brock Nelson", "Ryan Pulock",
                "Noah Dobson", "Anze Kopitar", "Drew Doughty", "Dustin Brown", "Tyler Toffoli",
                "Jeff Carter", "Tomas Hertl", "Logan Couture", "Brent Burns", "Erik Karlsson",
                "Joe Pavelski", "Jamie Benn", "Tyler Seguin", "Miro Heiskanen", "John Klingberg",
                "Filip Forsberg", "Ryan Johansen", "Matt Duchene", "Victor Hedman", "Brady Tkachuk",
                "Thomas Chabot", "Josh Norris", "Tim Stützle", "Pierre-Luc Dubois", "Cam Atkinson",
                "Travis Konecny", "Sean Couturier", "Claude Giroux", "Dylan Larkin", "Tyler Bertuzzi",
                "Filip Zadina", "Patrik Laine", "J.T. Miller", "Boone Jenner", "Sam Reinhart",
                "Roope Hintz", "Jason Robertson", "Alex DeBrincat", "Jonathan Huberdeau", "Sam Bennett"
        );

        Random random = new Random();

        for(int i = 0; i < (6*15); i++) {
            int goals = random.nextInt(50);
            int assists = random.nextInt(50);
            int penaltyMinutes = random.nextInt(100);
            StatsDto stat = StatsDto.builder()
                    .playerName(names.get(i % names.size()))
                    .teamName(teamNames.get(i % teamNames.size()))
                    .gamesPlayed(random.nextInt(82) + 1)
                    .goals(goals)
                    .assists(assists)
                    .points(goals+assists)
                    .penaltyMinutes(penaltyMinutes)
                    .build();
            staticSkaterStats.add(stat);
        }

        return staticSkaterStats;
    }

    public List<StatsDto> getGoalieStats() {
        if(staticGoalieStats != null) {
            return staticGoalieStats;
        }
        else {
            staticGoalieStats = new ArrayList<>();
        }
        List<String> teamNames = List.of(
                "Leafs",
                "Avalanche",
                "Vegas",
                "Red Wings",
                "Blackhawks",
                "Whalers"
        );

        List<String> names = List.of(
                "Marc-André Fleury",
                "Carey Price",
                "Connor Hellebuyck",
                "Andrei Vasilevskiy",
                "Robin Lehner",
                "Jordan Binnington"
        );

        Random random = new Random();

        for(int i = 0; i < (6); i++) {
            int goalsAgainst = random.nextInt(50) + 1;
            int penaltyMinutes = random.nextInt(100);
            StatsDto stat = StatsDto.builder()
                    .playerName(names.get(i % names.size()))
                    .teamName(teamNames.get(i % teamNames.size()))
                    .gamesPlayed(random.nextInt(82) + 1)
                    .penaltyMinutes(penaltyMinutes)
                    .goalsAgainst(goalsAgainst)
                    .wins(random.nextInt(50))
                    .losses(random.nextInt(50))
                    .ties(random.nextInt(10))
                    .shutouts(random.nextInt(10))
                    .build();
            staticGoalieStats.add(stat);
        }

        return staticGoalieStats;
    }
}
