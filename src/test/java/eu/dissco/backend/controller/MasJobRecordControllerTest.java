package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.givenClaims;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_SUFFIX;
import static eu.dissco.backend.utils.MasJobRecordUtils.MJR_URI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import eu.dissco.backend.database.jooq.enums.JobState;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class MasJobRecordControllerTest {

  private static final String MJR_PATH = "https://sandbox.dissco.tech" + MJR_URI;
  @Mock
  ApplicationProperties properties;
  @Mock
  private MasJobRecordService masJobRecordService;
  private MockHttpServletRequest mockRequest;
  private MasJobRecordController controller;
  @Mock
  private Authentication authentication;

  @BeforeEach
  void setup() {
    controller = new MasJobRecordController(MAPPER, properties, masJobRecordService);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(MJR_URI);
  }

  @Test
  void testGetMasJobRecord() throws Exception {
    // Given
    given(masJobRecordService.getMasJobRecordById(JOB_ID, MJR_PATH)).willReturn(
        new JsonApiWrapper(null, null));
    given(properties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.getMasJobRecord(PREFIX, JOB_SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMasJobRecordByCreatorId() throws Exception {
    // Given
    int pageNum = 1;
    int pageSize = 1;
    given(masJobRecordService.getMasJobRecordsByCreatorId(ORCID, MJR_PATH, pageNum, pageSize,
        JobState.FAILED)).willReturn(new JsonApiListResponseWrapper(null, null));
    given(properties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    givenAuthentication();

    // When
    var result = controller.getMasJobRecordsForCreator(pageNum, pageSize, JobState.FAILED,
        mockRequest, authentication);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testMarkMjrAsRunning() throws Exception {
    // When
    var result = controller.markMjrAsRunning(PREFIX, SUFFIX, PREFIX, JOB_SUFFIX);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void givenAuthentication() {
    var principal = mock(Jwt.class);
    given(authentication.getPrincipal()).willReturn(principal);
    given(principal.getClaims()).willReturn(givenClaims());
  }
}
