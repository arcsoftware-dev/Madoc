package dev.arcsoftware.madoc.config;

import dev.arcsoftware.madoc.model.ErrorSeverity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class FlywayConfig {

    private final String flywayLocations;
    private final String schema;

    private final DataSource dataSource;
    private final ResourcePatternResolver resourcePatternResolver;

    private static final String DDL_FILE_PREFIX = "V";
    private static final String CLASSPATH_LOCATION_PATTERN = "V*.sql";

    @Autowired
    public FlywayConfig(
            DataSource dataSource,
            ResourcePatternResolver resourcePatternResolver,
            @Value("${flyway.locations}") String flywayLocations,
            @Value("${postgres.datasource.schema}") String schema
    ) {
        this.flywayLocations = flywayLocations;
        this.schema = schema;
        this.dataSource = dataSource;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @PostConstruct
    public void migrate() throws IOException {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .defaultSchema(schema)
                .locations(flywayLocations)
                .load();
        flyway.migrate();
        logNotExecuted(flyway.info());
    }

    private void logNotExecuted(MigrationInfoService info) throws IOException {
        List<String> executedScripts = Arrays.stream(info.applied())
                .map(MigrationInfo::getScript)
                .filter(script -> script.startsWith(DDL_FILE_PREFIX))
                .toList();
        Arrays.stream(resourcePatternResolver.getResources(flywayLocations + File.separator + CLASSPATH_LOCATION_PATTERN))
                .map(Resource::getFilename)
                .filter(fileName -> !executedScripts.contains(fileName))
                .forEach( notExecutedScript -> log.error("{}: '{}' did not execute", ErrorSeverity.CRITICAL, notExecutedScript));
    }
}
