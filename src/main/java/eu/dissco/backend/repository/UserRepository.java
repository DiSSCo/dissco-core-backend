package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_USER;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.database.jooq.tables.records.NewUserRecord;
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
  private final Map<String, TableField<NewUserRecord, String>> attributeMapping = Map.of(
      "firstName", NEW_USER.FIRST_NAME,
      "lastName", NEW_USER.LAST_NAME,
      "email", NEW_USER.EMAIL,
      "orcid", NEW_USER.ORCID,
      "organisation", NEW_USER.ORGANIZATION
  );

  public User createNewUser(String id, User requestUser) {
    var createdTimestamp = Instant.now();
    return context.insertInto(NEW_USER)
        .set(NEW_USER.ID, id)
        .set(NEW_USER.FIRST_NAME, requestUser.firstName())
        .set(NEW_USER.LAST_NAME, requestUser.lastName())
        .set(NEW_USER.EMAIL, requestUser.email())
        .set(NEW_USER.ORCID, requestUser.orcid())
        .set(NEW_USER.ORGANIZATION, requestUser.organisation())
        .set(NEW_USER.CREATED, createdTimestamp)
        .set(NEW_USER.UPDATED, createdTimestamp)
        .returning().fetchOne().map(this::mapToUser);
  }

  private User mapToUser(Record dbRecord) {
    return new User(
        dbRecord.get(NEW_USER.FIRST_NAME),
        dbRecord.get(NEW_USER.LAST_NAME),
        dbRecord.get(NEW_USER.EMAIL),
        dbRecord.get(NEW_USER.ORCID),
        dbRecord.get(NEW_USER.ORGANIZATION)
    );
  }

  public User find(String id) {
    return context.selectFrom(NEW_USER).where(NEW_USER.ID.eq(id)).fetchOne(this::mapToUser);
  }

  public Optional<User> findOptional(String id){
    return context.selectFrom(NEW_USER).where(NEW_USER.ID.eq(id)).fetchOptional(this::mapToUser);
  }

  public User updateUser(String id, JsonNode attributes) {
    var updateQuery = context.update(NEW_USER)
        .set(NEW_USER.UPDATED, Instant.now());
    attributes.fields().forEachRemaining(field -> {
      if (attributeMapping.containsKey(field.getKey())) {
        updateQuery.set(attributeMapping.get(field.getKey()), field.getValue().asText());
      } else {
        log.warn("Could not map field: {} on a user attribute", field.getKey());
      }
    });
    return updateQuery.where(NEW_USER.ID.eq(id)).returning().fetchOne().map(this::mapToUser);
  }

  public void deleteUser(String id) {
    context.delete(NEW_USER).where(NEW_USER.ID.eq(id)).execute();
  }
}
