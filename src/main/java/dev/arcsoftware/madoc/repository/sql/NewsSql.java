package dev.arcsoftware.madoc.repository.sql;

public class NewsSql {
    public static final String GET_ALL_NEWS = """
        SELECT id, title, summary, content, author, created_at
        FROM madoc.news
        ORDER BY created_at DESC
        """;
}
