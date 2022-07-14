package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ID;
import static eu.dissco.backend.util.TestUtils.givenAnnotation;
import static eu.dissco.backend.util.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.util.TestUtils.loadResourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpecimenServiceTest {

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  @Mock
  private SpecimenRepository repository;
  @Mock
  private ElasticSearchRepository elasticSearchRepository;
  @Mock
  private AnnotationRepository annotationRepository;

  private SpecimenService service;

  @BeforeEach
  void setup() {
    service = new SpecimenService(repository, annotationRepository, elasticSearchRepository);
  }

  @Test
  void testGetSpecimen() throws IOException {
    // Given
    given(repository.getSpecimen(anyInt(), anyInt())).willReturn(List.of(givenDigitalSpecimen()));
    var expected = givenExpected();

    // When
    var result = service.getSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetSpecimenById() throws IOException {
    // Given
    given(repository.getSpecimenById(anyString())).willReturn(givenDigitalSpecimen());
    var expected = givenExpected();

    // When
    var result = service.getSpecimenById(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSearch() throws IOException {
    // Given
    given(elasticSearchRepository.search(anyString(), anyInt(), anyInt())).willReturn(
        List.of(givenExpected()));
    var expected = givenExpected();

    // When
    var result = service.search("Query", 1, 5);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  private DigitalSpecimen givenExpected() throws IOException {
    var expected = mapper.readValue(loadResourceFile("test-specimen.json"),
        DigitalSpecimen.class);
    expected.setId(ID);
    return expected;
  }

  @Test
  void testGetAnnotations() throws JsonProcessingException {
    // Given
    given(annotationRepository.getAnnotations(anyString())).willReturn(List.of(givenAnnotation()));

    // When
    var result = service.getAnnotations(ID);

    // Then
    assertThat(result).isEqualTo(List.of(givenAnnotation()));
  }

}
