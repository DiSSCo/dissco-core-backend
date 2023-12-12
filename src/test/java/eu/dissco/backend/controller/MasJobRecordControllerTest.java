package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.MJR_URI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.MasJobRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class MasJobRecordControllerTest {

  @Mock
  private MasJobRecordService masJobRecordService;
  @Mock
  ApplicationProperties properties;
  private MockHttpServletRequest mockRequest;
  private MasJobRecordController controller;

  private static final String MJR_PATH = "https://sandbox.dissco.tech" + MJR_URI;

  @BeforeEach
  void setup(){
    controller = new MasJobRecordController(MAPPER, properties, masJobRecordService);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(MJR_URI);
  }

  @Test
  void testGetMasJobRecord() throws Exception {
   // Given
    given(masJobRecordService.getMasJobRecordById(JOB_ID, MJR_PATH)).willReturn(new JsonApiWrapper(null, null));
    given(properties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");


    // When
    var result = controller.getMasJobRecord(JOB_ID, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMasJobRecordByCreatorId() {
    // Given
    int pageNum = 1;
    int pageSize = 1;
    given(masJobRecordService.getMasJobRecordsByCreator(ORCID, MJR_PATH, pageNum, pageSize, AnnotationState.FAILED)).willReturn(new JsonApiListResponseWrapper(null, null));
    given(properties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.getMasJobRecordsForCreator(ORCID, pageNum, pageSize, AnnotationState.FAILED, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testMarkMjrAsRunning() throws Exception {
    // When
    var result = controller.markMjrAsRunning(ID, JOB_ID);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
