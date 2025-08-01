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
         article1.setDate(LocalDateTime.of(2025, 9, 21, 12, 59, 0));

         NewsArticleDto article2 = new NewsArticleDto();
         article2.setTitle("League Fees 2025");
         article2.setContent("""
                 League fees for the 2025 season are now due. Please ensure you pay your fees by the end of the month to avoid penalties.
                 The deposited amount for the season is $100.
                 The total amount for the season is $400.  This must be paid in full by the end of September 2025.
                 """);
         article2.setSummary("""
                 League fees for the 2025 season are now due. Please ensure you pay your fees by the end of the month to avoid penalties.
                 The deposited amount for the season is $100.
                 The total amount for the season is $400.  This must be paid in full by the end of September 2025.
                 """);
         article2.setAuthor("Admin");
         article2.setDate(LocalDateTime.of(2025, 6, 23, 15, 23, 45));
         return List.of(article2, article1);
     }
}
