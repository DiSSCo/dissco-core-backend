package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.USER;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.database.jooq.tables.records.UserRecord;
import eu.dissco.backend.domain.User;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final DSLContext context;
  private final Map<String, TableField<UserRecord, String>> attributeMapping = Map.of(
      "firstName", USER.FIRST_NAME,
      "lastName", USER.LAST_NAME,
      "email", USER.EMAIL,
      "orcid", USER.ORCID,
      "organisation", USER.ORGANIZATION
  );

  public User createNewUser(String id, User requestUser) {
    var createdTimestamp = Instant.now();
    return context.insertInto(USER)
        .set(USER.ID, id)
        .set(USER.FIRST_NAME, requestUser.firstName())
        .set(USER.LAST_NAME, requestUser.lastName())
        .set(USER.EMAIL, requestUser.email())
        .set(USER.ORCID, requestUser.orcid())
        .set(USER.ORGANIZATION, requestUser.organisation())
        .set(USER.CREATED, createdTimestamp)
        .set(USER.UPDATED, createdTimestamp)
        .returning().fetchOne().map(this::mapToUser);
  }

  private User mapToUser(Record dbRecord) {
    return new User(
        dbRecord.get(USER.FIRST_NAME),
        dbRecord.get(USER.LAST_NAME),
        dbRecord.get(USER.EMAIL),
        dbRecord.get(USER.ORCID),
        dbRecord.get(USER.ORGANIZATION)
    );
  }

  public User find(String id) {
    return context.selectFrom(USER).where(USER.ID.eq(id)).fetchOne(this::mapToUser);
  }

  public Optional<User> findOptional(String id){
    return context.selectFrom(USER).where(USER.ID.eq(id)).fetchOptional(this::mapToUser);
  }

  public Optional<User> findOptionalFromOrcid(String orcid){
    return context.selectFrom(USER).where(USER.ORCID.eq(orcid)).fetchOptional(this::mapToUser);
  }

  public User updateUser(String id, JsonNode attributes) {
    var updateQuery = context.update(USER)
        .set(USER.UPDATED, Instant.now());
    attributes.fields().forEachRemaining(field -> {
      if (attributeMapping.containsKey(field.getKey())) {
        updateQuery.set(attributeMapping.get(field.getKey()), field.getValue().asText());
      } else {
        log.warn("Could not map field: {} on a user attribute", field.getKey());
      }
    });
    return updateQuery.where(USER.ID.eq(id)).returning().fetchOne().map(this::mapToUser);
  }

  public void deleteUser(String id) {
    context.delete(USER).where(USER.ID.eq(id)).execute();
  }
}
