package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class NewsRepository {

    // Placeholder for actual database interaction
     public List<NewsArticleDto> findAll() {
         NewsArticleDto article1 = new NewsArticleDto();
         article1.setTitle("Welcome to Madoc!");
         article1.setContent("Madoc is a new platform for managing your gaming news and standings.");
         article1.setSummary("Madoc is a new platform for managing your gaming news and standings.");
         article1.setAuthor("Admin");
         article1.setDate(LocalDateTime.of(2025, 8, 1, 14, 55, 0));

         NewsArticleDto article2 = new NewsArticleDto();
         article2.setTitle("League Fees 2025");
         article2.setContent("""
                 League fees for the 2025 season are now due. Please ensure you pay your fees by the August 15th.
                 The deposit amount for the season is $100.
                 The total amount for the season is $515.  Full payment date will be provided closer to the beginning of the season.
                 """);
         article2.setSummary("""
                 League fees for the 2025 season are now due. Please ensure you pay your fees by the August 15th.
                 The deposit amount for the season is $100.
                 The total amount for the season is $515.  Full payment date will be provided closer to the beginning of the season.
                 """);
         article2.setAuthor("Admin");
         article2.setDate(LocalDateTime.of(2025, 8, 11, 19, 1, 0));

         NewsArticleDto article3 = new NewsArticleDto();
         article3.setTitle("2025/2026 Season Information");
         article3.setContent("""
                 The returning player draft will be held on September 3rd, 2025 at 7PM at Oscars.
                 The 2025/2026 season will begin the week of September 14th 2025, with all games being played at Century Gardens.
                 Rosters and schedules will be finalized and send out the week of September 8th.
                 """);
         article3.setSummary("""
                 The returning player draft will be held on September 3rd, 2025 at 7PM at Oscars.
                 The 2025/2026 season will begin the week of September 14th 2025, with all games being played at Century Gardens.
                 Rosters and schedules will be finalized and sent out the week of September 8th.
                 """);
         article3.setAuthor("Admin");
         article3.setDate(LocalDateTime.of(2025, 8, 20, 13, 36, 0));

         return List.of(article3, article2, article1);
     }
}
