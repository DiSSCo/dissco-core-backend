package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AnnotationServiceTest {
  @Mock
  private AnnotationRepository repository;
  @Mock
  private AnnotationClient annotationClient;
  @Mock
  private ElasticSearchRepository elasticRepository;
  private AnnotationService service;
  private ObjectMapper mapper;

  @BeforeEach
  void setup(){
    mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setDefaultPropertyInclusion(Include.ALWAYS);

    service = new AnnotationService(repository, annotationClient, elasticRepository, mapper);
  }

  @Test
  void testGetAnnotationsForUser(){
    // Given
    String userId = "userId";
    int pageNumber = 1;
    int pageSize = 15;
    List<AnnotationResponse> expectedResponse = new ArrayList<>();
    for (int i = 0; i < pageSize; i++){
      expectedResponse.add(givenAnnotationResponse());
    }
    given(repository.getAnnotationsForUser(userId, pageNumber,pageSize)).willReturn(expectedResponse);

    // When
    var receivedResponse = service.getAnnotationsForUser(userId, pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations(){
    int pageNumber = 1;
    int pageSize = 15;
    List<AnnotationResponse> expectedResponse = new ArrayList<>();
    for (int i = 0; i < pageSize; i++){
      expectedResponse.add(givenAnnotationResponse());
    }
    given(repository.getAnnotations(pageNumber,pageSize)).willReturn(expectedResponse);

    // When
    var receivedResponse = service.getAnnotations(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    int pageNumber = 1;
    int pageSize = 15;
    List<AnnotationResponse> expectedResponse = new ArrayList<>();
    for (int i = 0; i < pageSize; i++){
      expectedResponse.add(givenAnnotationResponse());
    }
    given(elasticRepository.getLatestAnnotations(pageNumber,pageSize)).willReturn(expectedResponse);

    // When
    var receivedResponse = service.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testAnnotationNode() {
    // Given
    String id = "123";
    AnnotationResponse annotation = givenAnnotationResponse();
    given(repository.getAnnotation(id)).willReturn(annotation);

    // When
    //service.getAnnotationAndSpeciesName(id);


  }

}
