package eu.dissco.backend.controller;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.mockito.BDDMockito.given;


import eu.dissco.backend.domain.AnnotationResponse;
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
  void testGetLatestAnnotationsDefault() throws IOException {
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


}
