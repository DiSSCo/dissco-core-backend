package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.VIRTUAL_COLLECTION;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;
import static eu.dissco.backend.utils.ProxyUtils.removeHandleProxy;
import static org.jooq.impl.DSL.noCondition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.Tables;
import eu.dissco.backend.database.jooq.enums.CollectionType;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollection.LtcBasisOfScheme;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VirtualCollectionRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  private static CollectionType getLtcBasisOfScheme(VirtualCollection virtualCollection) {
    return switch (virtualCollection.getLtcBasisOfScheme()) {
      case LtcBasisOfScheme.REFERENCE_COLLECTION -> CollectionType.REFERENCE_COLLECTION;
      case LtcBasisOfScheme.COMMUNITY_COLLECTION -> CollectionType.COMMUNITY_COLLECTION;
    };
  }

  public void createVirtualCollection(VirtualCollection virtualCollection) {
    context.insertInto(VIRTUAL_COLLECTION)
        .set(VIRTUAL_COLLECTION.ID, removeHandleProxy(virtualCollection.getId()))
        .set(VIRTUAL_COLLECTION.VERSION, virtualCollection.getSchemaVersion())
        .set(VIRTUAL_COLLECTION.NAME, virtualCollection.getLtcCollectionName())
        .set(VIRTUAL_COLLECTION.COLLECTION_TYPE, getLtcBasisOfScheme(virtualCollection))
        .set(VIRTUAL_COLLECTION.CREATED, virtualCollection.getSchemaDateCreated().toInstant())
        .set(VIRTUAL_COLLECTION.MODIFIED, virtualCollection.getSchemaDateModified().toInstant())
        .set(VIRTUAL_COLLECTION.CREATOR, virtualCollection.getSchemaCreator().getId())
        .set(VIRTUAL_COLLECTION.DATA, mapToJSONB(virtualCollection))
        .execute();
  }

  private JSONB mapToJSONB(VirtualCollection virtualCollection) {
    try {
      return JSONB.valueOf(mapper.writeValueAsString(virtualCollection));
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to map virtual collection to jsonb", e);
    }
  }

  public void rollbackVirtualCollectionCreate(String id) {
    context.deleteFrom(Tables.VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(removeHandleProxy(id)))
        .execute();
  }

  public VirtualCollection getVirtualCollectionById(String id) {
    return context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(removeHandleProxy(id)))
        .fetchOne(this::mapToVirtualCollection);
  }

  private VirtualCollection mapToVirtualCollection(Record record1) {
    try {
      return mapper.readValue(record1.get(VIRTUAL_COLLECTION.DATA).data(), VirtualCollection.class);
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to convert jsonb to virtual collection", e);
    }
  }

  public List<VirtualCollection> getVirtualCollections(int pageNumber, int pageSize) {
    return virtualCollectionQuery(pageNumber, pageSize, List.of(noCondition()));
  }

  private List<VirtualCollection> virtualCollectionQuery(int pageNumber, int pageSize, List<Condition> conditions) {
    int offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(conditions)
        .and(VIRTUAL_COLLECTION.TOMBSTONED.isNull())
        .orderBy(VIRTUAL_COLLECTION.CREATED.desc())
        .limit(pageSizePlusOne)
        .offset(offset)
        .fetch(this::mapToVirtualCollection);
  }

  public List<VirtualCollection> getVirtualCollectionsForUser(String userId,
      int pageNumber, int pageSize) {
    var condition = VIRTUAL_COLLECTION.CREATOR.eq(userId);
    return virtualCollectionQuery(pageNumber, pageSize, List.of(condition));
  }

  public Optional<VirtualCollection> getActiveVirtualCollection(String id, String userId) {
    var query = context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(removeHandleProxy(id)))
        .and(VIRTUAL_COLLECTION.TOMBSTONED.isNull());
    if (userId != null) {
      query = query.and(VIRTUAL_COLLECTION.CREATOR.eq(userId));
    }
    return query.fetchOptional(this::mapToVirtualCollection);
  }

  public void tombstoneVirtualCollection(VirtualCollection tombstoneVirtualCollection) {
    context.update(VIRTUAL_COLLECTION)
        .set(VIRTUAL_COLLECTION.TOMBSTONED, tombstoneVirtualCollection.getOdsHasTombstoneMetadata().getOdsTombstoneDate().toInstant())
        .set(VIRTUAL_COLLECTION.MODIFIED, tombstoneVirtualCollection.getSchemaDateModified().toInstant())
        .set(VIRTUAL_COLLECTION.VERSION, tombstoneVirtualCollection.getSchemaVersion())
        .set(VIRTUAL_COLLECTION.DATA, mapToJSONB(tombstoneVirtualCollection))
        .where(VIRTUAL_COLLECTION.ID.eq(removeHandleProxy(tombstoneVirtualCollection.getId())))
        .execute();
  }

  public void updateVirtualCollection(VirtualCollection virtualCollection) {
    context.update(VIRTUAL_COLLECTION)
        .set(VIRTUAL_COLLECTION.VERSION, virtualCollection.getSchemaVersion())
        .set(VIRTUAL_COLLECTION.NAME, virtualCollection.getLtcCollectionName())
        .set(VIRTUAL_COLLECTION.COLLECTION_TYPE, getLtcBasisOfScheme(virtualCollection))
        .set(VIRTUAL_COLLECTION.CREATED, virtualCollection.getSchemaDateCreated().toInstant())
        .set(VIRTUAL_COLLECTION.MODIFIED, virtualCollection.getSchemaDateModified().toInstant())
        .set(VIRTUAL_COLLECTION.CREATOR, virtualCollection.getSchemaCreator().getId())
        .set(VIRTUAL_COLLECTION.DATA, mapToJSONB(virtualCollection))
        .where(VIRTUAL_COLLECTION.ID.eq(removeHandleProxy(virtualCollection.getId())))
        .execute();
  }
}
