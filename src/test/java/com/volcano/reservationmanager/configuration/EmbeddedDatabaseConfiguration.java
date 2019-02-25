package com.volcano.reservationmanager.configuration;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import javax.sql.DataSource;
import java.io.IOException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Profile("test")
@Configuration
public class EmbeddedDatabaseConfiguration {

	@Value("${spring.embedded-postgres.port:5433}")
	private String port;

	@Value("${spring.embedded-postgres.database:postgres_test}")
	private String database;

	@Value("${spring.embedded-postgres.username:postgres-user-test}")
	private String username;

	@Value("${spring.embedded-postgres.password:postgres-passwd-test}")
	private String password;

	@Value("${spring.embedded-postgres.data-dir:target}")
	private String dataDir;

	private PostgresProcess globalPostgresInstance;

	@Bean(destroyMethod = "stop")
	public PostgresProcess postgresProcess() throws IOException {
		if(!isNumeric(port)) {
			throw new IllegalStateException(format("To start a embedded postgres the property spring.embedded-postgres.port is not a number %s", port));
		}

		PostgresConfig postgresConfig = PostgresConfig.defaultWithDbName("postgres_test", username, password);

		PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
		PostgresExecutable exec = runtime.prepare(postgresConfig);
		globalPostgresInstance = exec.start();
		return globalPostgresInstance;
	}

	@Bean
	@DependsOn("postgresProcess")
	public DataSource dataSource(PostgresProcess postgresProcess) {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();

		PostgresConfig postgresConfig = postgresProcess.getConfig();
		dataSource.setUser(postgresConfig.credentials().username());
		dataSource.setPassword(postgresConfig.credentials().password());
		dataSource.setPortNumber(postgresConfig.net().port());
		dataSource.setServerName(postgresConfig.net().host());
		dataSource.setDatabaseName(postgresConfig.storage().dbName());

		return dataSource;
	}

	@EventListener({ ContextStoppedEvent.class, ContextClosedEvent.class })
	public void tearDown() {
		if(globalPostgresInstance != null) {
			globalPostgresInstance.stop();
		}
	}
}