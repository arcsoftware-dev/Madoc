package dev.arcsoftware.madoc.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewsArticleEntity {
    private long id;
    private String title;
    private String summary;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}
