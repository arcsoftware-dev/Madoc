package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.RuleDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class RuleRepository {
    private List<RuleDto> cachedRules;

    @PostConstruct
    public void loadRules() {
        log.info("Initializing RuleRepository and loading rules from CSV");
        cachedRules = loadRulesFromCsv();
    }

    public List<RuleDto> getAllRules() {
        return cachedRules;
    }

    private List<RuleDto> loadRulesFromCsv() {
        List<RuleDto> rules = new ArrayList<>();

        ClassPathResource rulesCsv = new ClassPathResource("data/rules.csv");
        final String[] HEADERS = {"title", "description"};
        try(Reader reader = new InputStreamReader(rulesCsv.getInputStream())) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                RuleDto ruleDto = new RuleDto(record.get("title"), record.get("description"));
                rules.add(ruleDto);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rules;
    }
}
