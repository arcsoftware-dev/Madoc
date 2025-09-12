package dev.arcsoftware.madoc.util;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.model.csv.RosterUploadRow;
import dev.arcsoftware.madoc.model.csv.ScheduleUploadRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileUploadParser {
    public List<RosterUploadRow> parseRosterCsv(byte[] csvFileBytes) {
        List<RosterUploadRow> rows = new ArrayList<>();

        final String[] HEADERS = {"#","Player","Team","Draft Rank","isRookie"};
        try(Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFileBytes))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                RosterUploadRow rosterUploadRow = new RosterUploadRow(
                        Integer.parseInt(record.get("#")),
                        record.get("Player"),
                        record.get("Team"),
                        DraftRank.valueOf(record.get("Draft Rank")),
                        record.get("isRookie").equalsIgnoreCase("true")
                );
                rows.add(rosterUploadRow);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rows;
    }

    public List<ScheduleUploadRow> parseScheduleCsv(byte[] csvFileBytes) {
        List<ScheduleUploadRow> rows = new ArrayList<>();

        final String[] HEADERS = {"Datetime", "Home Team", "Away Team"};
        try(Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFileBytes))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                ScheduleUploadRow scheduleUploadRow = new ScheduleUploadRow(
                        LocalDateTime.parse(record.get("Datetime")),
                        record.get("Home Team"),
                        record.get("Away Team")
                );
                rows.add(scheduleUploadRow);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rows;
    }


}
