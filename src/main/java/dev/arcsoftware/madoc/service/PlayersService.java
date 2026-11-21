package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.exception.ResultNotFoundException;
import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.model.payload.PlayerGameLogPage;
import dev.arcsoftware.madoc.model.payload.PlayerProfileDto;
import dev.arcsoftware.madoc.model.payload.PlayerProfileDto.PlayerSeasonStat;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.repository.PlayerRepository;
import dev.arcsoftware.madoc.repository.RosterRepository;
import dev.arcsoftware.madoc.repository.StatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlayersService {

    public static final int GAME_LOG_PAGE_SIZE = 5;
    private static final Pattern TRAILING_JERSEY_PATTERN = Pattern.compile("\\s*\\(#(\\d+)\\)\\s*$");

    private final RosterRepository rosterRepository;
    private final PlayerRepository playerRepository;
    private final StatsRepository statsRepository;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public PlayersService(
            RosterRepository rosterRepository,
            PlayerRepository playerRepository,
            StatsRepository statsRepository,
            SeasonMetadataService seasonMetadataService) {
        this.rosterRepository = rosterRepository;
        this.playerRepository = playerRepository;
        this.statsRepository = statsRepository;
        this.seasonMetadataService = seasonMetadataService;
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

    public boolean playerExistsById(Integer playerId) {
        return playerRepository.playerExistsById(playerId);
    }

    public void insertNewPlayer(PlayerEntity playerEntity) {
        rosterRepository.insertPlayer(playerEntity);
    }

    public Optional<Integer> findPlayerIdByName(String rawName) {
        String cleanName = stripTrailingJersey(rawName);
        if (cleanName.isBlank()) {
            return Optional.empty();
        }
        return playerRepository.findByFullNameIgnoreCase(cleanName).map(PlayerEntity::getId);
    }

    public PlayerProfileDto getPlayerProfile(int playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResultNotFoundException("Player not found with id: " + playerId));

        String fullName = buildFullName(player);
        List<RosterAssignmentDto> rosterHistory = rosterRepository.getAssignmentsByPlayerId(playerId);

        return buildProfile(playerId, fullName, null, rosterHistory);
    }

    public PlayerProfileDto getHistoricalPlayerProfileByName(String rawName) {
        String cleanName = stripTrailingJersey(rawName);
        Integer parsedJersey = extractTrailingJersey(rawName);
        if (cleanName.isBlank()) {
            throw new ResultNotFoundException("Player not found");
        }

        PlayerProfileDto profile = buildProfile(null, cleanName, parsedJersey, List.of());
        if (profile.getSkaterRegularStats().isEmpty()
                && profile.getSkaterPlayoffStats().isEmpty()
                && profile.getGoalieRegularStats().isEmpty()
                && profile.getGoaliePlayoffStats().isEmpty()) {
            throw new ResultNotFoundException("Player not found: " + cleanName);
        }
        return profile;
    }

    private PlayerProfileDto buildProfile(Integer playerId,
                                          String fullName,
                                          Integer fallbackJersey,
                                          List<RosterAssignmentDto> rosterHistory) {
        List<Integer> years = seasonMetadataService.getAllSeasonYears().stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        Set<String> goalieYearTeamKeys = rosterHistory.stream()
                .filter(ra -> ra.getPosition() == Position.GOALIE && ra.getSeasonYear() != null && ra.getTeamName() != null)
                .map(ra -> ra.getSeasonYear() + "|" + ra.getTeamName().toLowerCase())
                .collect(Collectors.toSet());

        List<PlayerSeasonStat> skaterRegular = new ArrayList<>();
        List<PlayerSeasonStat> skaterPlayoffs = new ArrayList<>();
        List<PlayerSeasonStat> goalieRegular = new ArrayList<>();
        List<PlayerSeasonStat> goaliePlayoffs = new ArrayList<>();

        for (int year : years) {
            for (StatsDto stat : statsRepository.getSkaterStats(year, SeasonType.REGULAR_SEASON)) {
                if (matchesPlayer(stat, playerId, fullName)) {
                    boolean isGoalieRow = goalieYearTeamKeys.contains(year + "|" + safeLower(stat.getTeamName()));
                    if (!isGoalieRow || hasSkaterScoring(stat)) {
                        skaterRegular.add(toSeasonStat(year, stat));
                    }
                }
            }
            for (StatsDto stat : statsRepository.getSkaterStats(year, SeasonType.PLAYOFFS)) {
                if (matchesPlayer(stat, playerId, fullName) && hasSkaterActivity(stat)) {
                    boolean isGoalieRow = goalieYearTeamKeys.contains(year + "|" + safeLower(stat.getTeamName()));
                    if (!isGoalieRow || hasSkaterScoring(stat)) {
                        skaterPlayoffs.add(toSeasonStat(year, stat));
                    }
                }
            }
            for (StatsDto stat : statsRepository.getGoalieStats(year, SeasonType.REGULAR_SEASON)) {
                if (matchesPlayer(stat, playerId, fullName)) {
                    goalieRegular.add(toSeasonStat(year, stat));
                }
            }
            for (StatsDto stat : statsRepository.getGoalieStats(year, SeasonType.PLAYOFFS)) {
                if (matchesPlayer(stat, playerId, fullName) && hasGoalieActivity(stat)) {
                    goaliePlayoffs.add(toSeasonStat(year, stat));
                }
            }
        }

        RosterAssignmentDto latestRoster = rosterHistory.isEmpty() ? null : rosterHistory.getFirst();
        PlayerSeasonStat latestStat = !goalieRegular.isEmpty() && skaterRegular.isEmpty()
                ? goalieRegular.getFirst()
                : (!skaterRegular.isEmpty() ? skaterRegular.getFirst() : (!goalieRegular.isEmpty() ? goalieRegular.getFirst() : null));

        String currentTeam = latestRoster != null ? latestRoster.getTeamName()
                : (latestStat != null ? latestStat.getTeamName() : null);
        Integer currentSeasonYear = latestRoster != null ? latestRoster.getSeasonYear()
                : (latestStat != null ? latestStat.getYear() : null);
        Integer currentJersey = latestRoster != null ? latestRoster.getJerseyNumber() : fallbackJersey;
        Position currentPosition = latestRoster != null ? latestRoster.getPosition()
                : (!goalieRegular.isEmpty() && skaterRegular.isEmpty() ? Position.GOALIE : null);

        boolean primaryGoalie = currentPosition == Position.GOALIE
                || (!goalieRegular.isEmpty() && skaterRegular.isEmpty());

        Set<Integer> distinctYears = new HashSet<>();
        rosterHistory.forEach(r -> { if (r.getSeasonYear() != null) distinctYears.add(r.getSeasonYear()); });
        skaterRegular.forEach(s -> distinctYears.add(s.getYear()));
        skaterPlayoffs.forEach(s -> distinctYears.add(s.getYear()));
        goalieRegular.forEach(s -> distinctYears.add(s.getYear()));
        goaliePlayoffs.forEach(s -> distinctYears.add(s.getYear()));

        PlayerGameLogPage initialGameLogs = playerId != null
                ? statsRepository.getPlayerGameLogs(playerId, 0, GAME_LOG_PAGE_SIZE)
                : new PlayerGameLogPage(List.of(), 0, 0, GAME_LOG_PAGE_SIZE);

        return PlayerProfileDto.builder()
                .playerId(playerId)
                .fullName(fullName)
                .currentTeam(currentTeam)
                .currentSeasonYear(currentSeasonYear)
                .currentJerseyNumber(currentJersey)
                .currentPosition(currentPosition)
                .currentDraftPosition(latestRoster != null ? latestRoster.getDraftPosition() : null)
                .currentRookie(latestRoster != null && latestRoster.isRookie())
                .primaryGoalie(primaryGoalie)
                .seasonsPlayed(distinctYears.size())
                .rosterHistory(rosterHistory)
                .skaterRegularStats(skaterRegular)
                .skaterPlayoffStats(skaterPlayoffs)
                .skaterRegularTotals(sumStats(skaterRegular))
                .skaterPlayoffTotals(sumStats(skaterPlayoffs))
                .goalieRegularStats(goalieRegular)
                .goaliePlayoffStats(goaliePlayoffs)
                .goalieRegularTotals(sumStats(goalieRegular))
                .goaliePlayoffTotals(sumStats(goaliePlayoffs))
                .gameLogs(initialGameLogs.getGames())
                .totalGameLogs(initialGameLogs.getTotalGames())
                .build();
    }

    public PlayerGameLogPage getPlayerGameLogs(int playerId, int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.min(Math.max(1, limit), 50);
        return statsRepository.getPlayerGameLogs(playerId, safeOffset, safeLimit);
    }

    private boolean matchesPlayer(StatsDto stat, Integer playerId, String fullName) {
        if (stat == null) {
            return false;
        }
        if (playerId != null && stat.getPlayerId() != null) {
            return playerId.equals(stat.getPlayerId());
        }
        return stripTrailingJersey(stat.getPlayerName()).equalsIgnoreCase(fullName);
    }

    private boolean hasSkaterScoring(StatsDto s) {
        return s.getGoals() > 0 || s.getAssists() > 0 || s.getPoints() > 0;
    }

    private boolean hasSkaterActivity(StatsDto s) {
        return s.getGamesPlayed() > 0 || s.getGoals() > 0 || s.getAssists() > 0 || s.getPenaltyMinutes() > 0;
    }

    private boolean hasGoalieActivity(StatsDto s) {
        return s.getGamesPlayed() > 0 || s.getWins() > 0 || s.getLosses() > 0 || s.getTies() > 0 || s.getPenaltyMinutes() > 0;
    }

    private PlayerSeasonStat toSeasonStat(int year, StatsDto s) {
        return PlayerSeasonStat.builder()
                .year(year)
                .teamName(s.getTeamName())
                .gamesPlayed(s.getGamesPlayed())
                .goals(s.getGoals())
                .assists(s.getAssists())
                .points(s.getPoints())
                .penaltyMinutes(s.getPenaltyMinutes())
                .wins(s.getWins())
                .losses(s.getLosses())
                .ties(s.getTies())
                .shutouts(s.getShutouts())
                .goalsAgainst(s.getGoalsAgainst())
                .build();
    }

    private PlayerSeasonStat sumStats(List<PlayerSeasonStat> rows) {
        int gp = 0, g = 0, a = 0, pts = 0, pim = 0;
        int w = 0, l = 0, t = 0, so = 0, ga = 0;
        for (PlayerSeasonStat r : rows) {
            gp += r.getGamesPlayed();
            g += r.getGoals();
            a += r.getAssists();
            pts += r.getPoints();
            pim += r.getPenaltyMinutes();
            w += r.getWins();
            l += r.getLosses();
            t += r.getTies();
            so += r.getShutouts();
            ga += r.getGoalsAgainst();
        }
        return PlayerSeasonStat.builder()
                .year(0)
                .teamName("Career")
                .gamesPlayed(gp)
                .goals(g)
                .assists(a)
                .points(pts)
                .penaltyMinutes(pim)
                .wins(w)
                .losses(l)
                .ties(t)
                .shutouts(so)
                .goalsAgainst(ga)
                .build();
    }

    private String buildFullName(PlayerEntity player) {
        String first = player.getFirstName() != null ? player.getFirstName().trim() : "";
        String last = player.getLastName() != null ? player.getLastName().trim() : "";
        return (first + " " + last).trim();
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public static String stripTrailingJersey(String rawName) {
        if (rawName == null) {
            return "";
        }
        return TRAILING_JERSEY_PATTERN.matcher(rawName.trim()).replaceFirst("").trim();
    }

    private Integer extractTrailingJersey(String rawName) {
        if (rawName == null) {
            return null;
        }
        Matcher m = TRAILING_JERSEY_PATTERN.matcher(rawName.trim());
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
