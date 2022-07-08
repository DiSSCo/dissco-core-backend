package eu.dissco.backend.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class BaseRepositoryIT {

  private static final DockerImageName IMAGE_NAME = DockerImageName.parse("postgres:13.7")
      .asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE);

  @Container
  private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>(IMAGE_NAME);

  private HikariDataSource dataSource;

  protected DSLContext context;

  @BeforeEach
  void prepareDatabase() {
    dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(CONTAINER.getJdbcUrl());
    dataSource.setUsername(CONTAINER.getUsername());
    dataSource.setPassword(CONTAINER.getPassword());
    dataSource.setMaximumPoolSize(1);
    dataSource.setConnectionInitSql(CONTAINER.getTestQueryString());
    Flyway.configure().mixed(true).dataSource(dataSource).load().migrate();
    context = new DefaultDSLContext(dataSource, SQLDialect.POSTGRES);
  }
}


