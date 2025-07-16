package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.database.jooq.Tables.VIRTUAL_COLLECTION;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.utils.VirtualCollectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    var virtualCollection = VirtualCollectionUtils.givenVirtualCollection(HANDLE + ID);
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
    var expected = Pair.of(11, fullAddedList.subList(0, 5));

    // When
    var result = repository.getVirtualCollection(1, 5);

    // Then
    assertThat(result.getLeft()).isEqualTo(expected.getLeft());
    assertThat(result.getRight()).containsExactlyInAnyOrderElementsOf(expected.getRight());
  }

  @Test
  void testGetVirtualCollectionForCreator() {
    // Given
    populateTable();
    var virtualCollection = VirtualCollectionUtils.givenVirtualCollection(HANDLE + ID);

    // When
    var result = repository.getVirtualCollectionForUser(ORCID, 1, 10);

    // Then
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(List.of(virtualCollection));
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
