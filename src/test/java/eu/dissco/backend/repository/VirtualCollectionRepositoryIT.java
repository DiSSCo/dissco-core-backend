package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.database.jooq.Tables.VIRTUAL_COLLECTION;
import static eu.dissco.backend.utils.HandleProxyUtils.removeProxy;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTombstoneVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.schema.VirtualCollection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VirtualCollectionRepositoryIT extends BaseRepositoryIT {

  private VirtualCollectionRepository repository;

  @BeforeEach
  void setup() {
    repository = new VirtualCollectionRepository(MAPPER, context);
  }

  @AfterEach
  void destroy() {
    context.truncate(VIRTUAL_COLLECTION).execute();
  }

  @Test
  void testGetVirtualCollectionById() {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    populateTable();

    // When
    var result = repository.getVirtualCollectionById(virtualCollection.getId());

    // Then
    assertThat(result).isEqualTo(virtualCollection);
  }

  @Test
  void testGetVirtualCollectionByIdNotFound() {
    // Given
    var fullAddedList = populateTable();
    var expected = Pair.of(11, fullAddedList.subList(0, 6));

    // When
    var result = repository.getVirtualCollections(1, 5);

    // Then
    assertThat(result).containsExactlyInAnyOrderElementsOf(expected.getRight());
  }

  @Test
  void testGetVirtualCollectionsForCreator() {
    // Given
    populateTable();
    var virtualCollection = List.of(givenVirtualCollection(HANDLE + ID));

    // When
    var result = repository.getVirtualCollectionsForUser(ORCID, 1, 10);

    // Then
    assertThat(result).hasSize(1).isEqualTo(virtualCollection);
  }

  @Test
  void testRollbackVirtualCollectionCreate() {
    // Given
    populateTable();

    // When
    repository.rollbackVirtualCollectionCreate(HANDLE + ID);

    // Then
    var virtualCollection = repository.getVirtualCollectionById(ID);
    assertThat(virtualCollection).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {ORCID})
  @NullSource
  void testGetActiveVirtualCollection(String userId) {
    // Given
    populateTable();

    // When
    var result = repository.getActiveVirtualCollection(ID, userId);

    // Then
    assertThat(result).contains(givenVirtualCollection(HANDLE + ID));
  }

  @Test
  void testGetActiveVirtualCollection() {
    // Given
    populateTable();

    // When
    var result = repository.getActiveVirtualCollection(ID, "Random username");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void testTombstoneVirtualCollection() {
    // Given
    var tombStonedvirtualCollection = givenTombstoneVirtualCollection();
    populateTable();

    // When
    repository.tombstoneVirtualCollection(tombStonedvirtualCollection);

    // Then
    var result = context.select(VIRTUAL_COLLECTION.TOMBSTONED)
        .from(VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(removeProxy(tombStonedvirtualCollection.getId())))
        .fetchOne(VIRTUAL_COLLECTION.TOMBSTONED);
    assertThat(result).isEqualTo(CREATED);
  }

  @Test
  void testUpdateVirtualCollection() throws JsonProcessingException {
    // Given
    populateTable();
    var updatedRecord = givenVirtualCollection(HANDLE + ID, ORCID, "An updated name", 2);

    // When
    repository.updateVirtualCollection(updatedRecord);

    // Then
    var result = context.select(VIRTUAL_COLLECTION.DATA)
        .from(VIRTUAL_COLLECTION)
        .where(VIRTUAL_COLLECTION.ID.eq(removeProxy(updatedRecord.getId())))
        .fetchOne(VIRTUAL_COLLECTION.DATA);
    assertThat(MAPPER.readValue(result.data(), VirtualCollection.class)).isEqualTo(updatedRecord);
  }

  private List<VirtualCollection> populateTable() {
    var result = new ArrayList<VirtualCollection>();
    var virtualCollectionDefault = givenVirtualCollection(HANDLE + ID, ORCID);
    repository.createVirtualCollection(virtualCollectionDefault);
    result.add(virtualCollectionDefault);
    for (int i = 0; i < 10; i++) {
      var virtualCollection = givenVirtualCollection(HANDLE + ID.substring(0, ID.length() - 1) + i,
          ORCID.substring(0, ORCID.length() - 1) + i);
      repository.createVirtualCollection(virtualCollection);
      result.add(virtualCollection);
    }
    return result;
  }

}
