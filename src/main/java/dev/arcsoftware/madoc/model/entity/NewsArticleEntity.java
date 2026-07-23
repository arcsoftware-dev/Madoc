package dev.arcsoftware.madoc.model.entity;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewsArticleEntity {
    private long id;
    private String title;
    private String summary;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    
    public NewsArticleEntity(long id, String title, String summary, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }
}
