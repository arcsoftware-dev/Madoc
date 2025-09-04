package dev.arcsoftware.madoc.repository.sql;

public class RulesSql {
    public static final String GET_ALL_RULES = """
        SELECT id, title, description, created_at, updated_at
        FROM madoc.rules
        ORDER BY id ASC
        """;
}
