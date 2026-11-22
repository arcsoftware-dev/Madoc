package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.model.entity.GameEntity;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.repository.GameRepository;
import dev.arcsoftware.madoc.repository.RosterRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentServicePdfTest {

    private GameRepository gameRepository;
    private RosterRepository rosterRepository;
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        rosterRepository = mock(RosterRepository.class);

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addDialect(new Java8TimeDialect());
        templateEngine.setTemplateResolver(resolver);

        documentService = new DocumentService(gameRepository, rosterRepository, templateEngine);
    }

    @Test
    void generatesExactlyThreePagesPerGameWithCorrectContent() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 20);
        List<GameEntity> games = List.of(
                buildGame(1, date.atTime(19, 0), "Flames", "Leafs"),
                buildGame(2, date.atTime(20, 0), "Jets", "Oilers"),
                buildGame(3, date.atTime(21, 0), "Habs", "Canucks")
        );

        when(gameRepository.findByDate(date)).thenReturn(games);
        for (String team : List.of("Flames", "Leafs", "Jets", "Oilers", "Habs", "Canucks")) {
            when(rosterRepository.getAssignmentsByYearAndTeam(2026, team)).thenReturn(buildRoster(team, 14));
        }

        Pair<String, byte[]> result = documentService.getPregamePackagePdf(date);
        assertThat(result.left()).isEqualTo("PregamePackage-20260920");
        assertThat(result.right()).isNotEmpty();

        try (PDDocument pdf = Loader.loadPDF(result.right())) {
            // 1 room sheet + 3 games x (1 gamesheet + 1 home sign-in + 1 away sign-in) = exactly 10 pages
            assertThat(pdf.getNumberOfPages()).isEqualTo(10);

            var roomBox = pdf.getPage(0).getMediaBox();
            assertThat(roomBox.getHeight()).isGreaterThan(roomBox.getWidth());
            var gamesheetBox = pdf.getPage(1).getMediaBox();
            assertThat(gamesheetBox.getHeight()).isGreaterThan(gamesheetBox.getWidth());

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String page1 = stripper.getText(pdf);
            assertThat(page1).contains("Home");
            assertThat(page1).contains("Away");
            assertThat(page1).contains("Room");
            assertThat(page1).contains("Flames");
            assertThat(page1).contains("Leafs");
            assertThat(page1).contains("Jets");
            assertThat(page1).contains("Oilers");
            assertThat(page1).contains("Habs");
            assertThat(page1).contains("Canucks");
            assertThat(page1).contains("7 PM");
            assertThat(page1).contains("8 PM");
            assertThat(page1).contains("9 PM");

            stripper.setStartPage(2);
            stripper.setEndPage(2);
            String page2 = stripper.getText(pdf);
            assertThat(page2).contains("Madoc Mens Hockey League");
            assertThat(page2).contains("2026/2027");
            assertThat(page2).contains("HOME: FLAMES");
            assertThat(page2).contains("AWAY: LEAFS");
            assertThat(page2).contains("Flames Player 1");
            assertThat(page2).contains("Leafs Player 14");
            assertThat(page2).contains("*PS = Penalty Shot");

            stripper.setStartPage(3);
            stripper.setEndPage(3);
            String page3 = stripper.getText(pdf);
            assertThat(page3).contains("Madoc Sign In Sheet");
            assertThat(page3).contains("HOME TEAM");
            assertThat(page3).contains("Flames");
            assertThat(page3).contains("Flames Player 1");

            stripper.setStartPage(4);
            stripper.setEndPage(4);
            String page4 = stripper.getText(pdf);
            assertThat(page4).contains("Madoc Sign In Sheet");
            assertThat(page4).contains("AWAY TEAM");
            assertThat(page4).contains("Leafs");
            assertThat(page4).contains("Leafs Player 1");
        }
    }

    private GameEntity buildGame(int id, LocalDateTime time, String homeName, String awayName) {
        TeamEntity home = new TeamEntity();
        home.setId(id * 10 + 1);
        home.setTeamName(homeName);
        home.setYear(2026);

        TeamEntity away = new TeamEntity();
        away.setId(id * 10 + 2);
        away.setTeamName(awayName);
        away.setYear(2026);

        GameEntity game = new GameEntity();
        game.setId(id);
        game.setYear(2026);
        game.setSeasonType(SeasonType.REGULAR_SEASON);
        game.setVenue(Arena.CENTURY);
        game.setGameTime(time);
        game.setHomeTeam(home);
        game.setAwayTeam(away);
        return game;
    }

    private List<RosterAssignmentDto> buildRoster(String teamName, int count) {
        DraftRank[] ranks = DraftRank.values();
        List<RosterAssignmentDto> roster = new ArrayList<>();
        for (int i = 0; i < Math.min(count, ranks.length); i++) {
            RosterAssignmentDto dto = new RosterAssignmentDto();
            dto.setTeamName(teamName);
            dto.setDraftPosition(ranks[i]);
            dto.setJerseyNumber(i + 11);
            dto.setFullName(teamName + " Player " + (i + 1));
            dto.setActive(true);
            roster.add(dto);
        }
        return roster;
    }
}
