package eu.dissco.backend.controller;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.mockito.BDDMockito.given;


import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AnnotationControllerTest {

  @Mock
  private AnnotationService service;
  private AnnotationController controller;

  @BeforeEach
  void setup() { controller = new AnnotationController(service); }

  @Test
  void testGetAnnotation(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    String id = prefix + "/" + postfix;
    var expectedResponse = ResponseEntity.ok(givenAnnotationResponse());
    given(service.getAnnotation(id)).willReturn(givenAnnotationResponse());

    // When
    var receivedResponse = controller.getAnnotation(prefix, postfix);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    int pageNumber = 1;
    int pageSize = 30;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = pageNumber * pageSize;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }
    given(service.getLatestAnnotations(pageNumber, pageSize)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotationsDefaultPagination() throws IOException {
    // Given
    int pageNumberDefault = 0;
    int pageSizeDefault = 10;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = 10;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }

    given(service.getLatestAnnotations(pageNumberDefault, pageSizeDefault)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getLatestAnnotations(null, null);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotationsJsonResponse() throws IOException {
    // Given
    String path = "sandbox.dissco.tech/api/v1/annotations/latest/json";
    int pageNumber = 1;
    int pageSize = 11;
    int totalPageCount = 100;
    var expectedJson = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getLatestAnnotationsJsonResponse(pageNumber, pageSize, path)).willReturn(expectedJson);

    // When
    var receivedResponse = controller.getLatestAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationVersion() {
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    int version = 1;
    String id = prefix + "/" + postfix;
    var expectedResponse = ResponseEntity.ok(givenAnnotationResponse());
    given(service.getAnnotationVersion(id, version)).willReturn(givenAnnotationResponse());

    // When
    var receivedResponse = controller.getAnnotation(prefix, postfix, version);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsJsonResponse(){
    int pageNumber = 1;
    int pageSize = 11;
    int totalPageCount = 100;
    String path = "sandbox.dissco.tech/api/v1/annotations/all/json";
    var expectedJson = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getAnnotationsJsonResponse(pageNumber, pageSize, path)).willReturn(expectedJson);

    // When
    var receivedResponse = controller.getAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations(){
    // Given
    int pageNumber = 1;
    int pageSize = 30;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = pageNumber * pageSize;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }

    given(service.getAnnotations(pageNumber, pageSize)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getAnnotations(pageNumber, pageSize);

    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsDefault(){
    // Given
    int pageNumberDefault = 0;
    int pageSizeDefault = 10;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = 10;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }

    given(service.getAnnotations(pageNumberDefault, pageSizeDefault)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getAnnotations(null, null);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

}
