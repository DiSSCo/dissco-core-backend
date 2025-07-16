package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.VIRTUAL_COLLECTION;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;
import static eu.dissco.backend.utils.HandleProxyUtils.removeProxy;
import static org.jooq.impl.DSL.noCondition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.Tables;
import eu.dissco.backend.database.jooq.enums.CollectionType;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollection.LtcBasisOfScheme;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Record1;
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
        .set(VIRTUAL_COLLECTION.ID, virtualCollection.getId())
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
        .where(VIRTUAL_COLLECTION.ID.eq(removeProxy(id)))
        .execute();
  }

  public VirtualCollection getVirtualCollectionById(String id) {
    return context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(id))
        .fetchOne(this::mapToVirtualCollection);
  }

  private VirtualCollection mapToVirtualCollection(Record record1) {
    try {
      return mapper.readValue(record1.get(VIRTUAL_COLLECTION.DATA).data(), VirtualCollection.class);
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to convert jsonb to virtual collection", e);
    }
  }

  public Pair<Integer, List<VirtualCollection>> getVirtualCollection(int pageNumber, int pageSize) {
    var virtualCollection = getVirtualCollectionQuery(pageNumber, pageSize, List.of(noCondition()));
    return addTotalCount(virtualCollection, List.of(noCondition()));
  }

  private List<VirtualCollection> getVirtualCollectionQuery(int pageNumber, int pageSize, List<Condition> conditions) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(conditions)
        .orderBy(VIRTUAL_COLLECTION.CREATED.desc())
        .limit(pageSize)
        .offset(offset)
        .fetch(this::mapToVirtualCollection);
  }

  private Pair<Integer, List<VirtualCollection>> addTotalCount(
      List<VirtualCollection> virtualCollection, List<Condition> conditions) {
    var totalCount = context.selectCount()
        .from(VIRTUAL_COLLECTION)
        .where(conditions)
        .fetchOne(Record1::value1);
    return Pair.of(totalCount, virtualCollection);
  }

  public Pair<Integer, List<VirtualCollection>> getVirtualCollectionForUser(String userId,
      int pageNumber, int pageSize) {
    var condition = VIRTUAL_COLLECTION.CREATOR.eq(userId);
    var virtualCollection = getVirtualCollectionQuery(pageNumber, pageSize, List.of(condition));
    return addTotalCount(virtualCollection, List.of(condition));
  }
}
