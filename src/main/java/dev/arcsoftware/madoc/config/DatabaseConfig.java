package dev.arcsoftware.madoc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @FlywayDataSource
    @Bean
    public DataSource dataSource(
            @Value("${postgres.datasource.jdbcUrl}") String jdbcUrl,
            @Value("${postgres.datasource.username}") String username,
            @Value("${postgres.datasource.password}") String password,
            @Value("${postgres.datasource.schema}") String schema
    ){
        HikariDataSource dataSource = DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .type(HikariDataSource.class)
                .build();
        dataSource.setSchema(schema);
        return dataSource;
    }

    @Bean
    public JdbcClient jdbcClient(DataSource dataSource){
        return JdbcClient.create(new NamedParameterJdbcTemplate(dataSource));
    }
}
