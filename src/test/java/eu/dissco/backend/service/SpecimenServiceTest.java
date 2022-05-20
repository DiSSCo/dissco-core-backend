package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ID;
import static eu.dissco.backend.util.TestUtils.givenCordraObject;
import static eu.dissco.backend.util.TestUtils.loadResourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.repository.CordraRepository;
import java.io.IOException;
import java.util.List;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.CordraObject;
import net.cnri.cordra.api.SearchResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpecimenServiceTest {

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  @Mock
  private CordraRepository repository;
  @Mock
  private SearchResults<CordraObject> searchResults;

  private SpecimenService service;

  @BeforeEach
  void setup() {
    service = new SpecimenService(repository, mapper);
  }

  @Test
  void testGetSpecimen() throws CordraException, IOException {
    // Given
    given(searchResults.iterator()).willReturn(List.of(givenCordraObject()).iterator());
    given(repository.getSpecimen(anyInt(), anyInt())).willReturn(searchResults);
    var expected = givenExpected();

    // When
    var result = service.getSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetSpecimenById() throws CordraException, IOException {
    // Given
    given(repository.getSpecimenById(anyString())).willReturn(givenCordraObject());
    var expected = givenExpected();

    // When
    var result = service.getSpecimenById(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSearch() throws CordraException, IOException {
    // Given
    given(searchResults.iterator()).willReturn(List.of(givenCordraObject()).iterator());
    given(repository.search(anyString(), anyInt(), anyInt())).willReturn(searchResults);
    var expected = givenExpected();

    // When
    var result = service.search("Query", 1, 5);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  private DigitalSpecimen givenExpected() throws IOException {
    var expected = mapper.readValue(loadResourceFile("test-object.json"),
        DigitalSpecimen.class);
    expected.setId(ID);
    return expected;
  }


}
