package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.TestUtils;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
  private MockedStatic<Instant> mockedStatic;
  private Instant instant;

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
  void testGetAnnotationsForUserJsonResponse(){
    // Given
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    int pageNumber = 1;
    int pageSize = 15;
    int totalPageCount = 100;
    String path = "sandbox.dissco.tech/api/v1/annotations/creator/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount,
        userId, annotationId);
    given(repository.getAnnotationsForUserJsonResponse(userId, pageNumber, pageSize)).willReturn(
        TestUtils.givenAnnotationJsonApiDataList(pageSize, userId, annotationId));
    given(repository.getAnnotationsCountForUser(userId, pageSize)).willReturn(totalPageCount);

    // When
    var receivedResponse = service.getAnnotationsForUserJsonResponse(userId, pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotation(){
    // Given
    String id = "id";
    var expectedResponse = givenAnnotationResponse();
    given(repository.getAnnotation(id)).willReturn(expectedResponse);

    // When
    var receivedResponse = service.getAnnotation(id);

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
  void testGetAnnotationsJsonResponse(){
    int pageNumber = 1;
    int pageSize = 15;
    int totalPageCount = 100;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    String path = "sandbox.dissco.tech/api/v1/annotations/all/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount,
        userId, annotationId);
    given(repository.getAnnotationsJsonResponse(pageNumber, pageSize)).willReturn(
        TestUtils.givenAnnotationJsonApiDataList(pageSize, userId, annotationId));
    given(repository.getAnnotationsCountGlobal(pageSize)).willReturn(totalPageCount);

    // When
    var receivedResponse = service.getAnnotationsJsonResponse(pageNumber, pageSize, path);

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
  void testGetLatestAnnotationsJsonResponse() throws IOException {
    int pageNumber = 1;
    int pageSize = 15;
    int totalPageCount = 100;
    String path = "sandbox.dissco.tech/api/v1/annotations/latest/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount,
        USER_ID_TOKEN, "123");
    given(elasticRepository.getLatestAnnotationsJsonResponse(pageNumber, pageSize)).willReturn(
        TestUtils.givenAnnotationJsonApiDataList(pageSize, USER_ID_TOKEN, "123"));
    given(repository.getAnnotationsCountGlobal(pageSize)).willReturn(totalPageCount);

    // When
    var receivedResponse = service.getLatestAnnotationsJsonResponse(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

}
