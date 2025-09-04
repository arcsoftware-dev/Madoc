package dev.arcsoftware.madoc.model.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewsArticleDto {
    private long id;
    private String title;
    private String summary;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}
