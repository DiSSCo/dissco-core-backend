package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.givenAggregationMap;
import static eu.dissco.backend.TestUtils.givenClaims;
import static eu.dissco.backend.TestUtils.givenTaxonAggregationMap;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_PATH;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_URI;
import static eu.dissco.backend.utils.SpecimenUtils.givenDigitalSpecimenJsonApiDataList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.DigitalSpecimenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.MultiValueMapAdapter;

@ExtendWith(MockitoExtension.class)
class DigitalSpecimenControllerTest {

  MockHttpServletRequest mockRequest;
  @Mock
  Authentication authentication;
  @Mock
  private DigitalSpecimenService service;
  @Mock
  private ApplicationProperties applicationProperties;
  private DigitalSpecimenController controller;

  @BeforeEach
  void setup() {
    controller = new DigitalSpecimenController(applicationProperties, MAPPER, service);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(SPECIMEN_URI);
  }

  @Test
  void testGetSpecimen() throws Exception {
    // When
    var result = controller.getSpecimen(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetLatestSpecimen() throws Exception {
    var result = controller.getLatestSpecimen(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenById() {
    // When
    var result = controller.getSpecimenById(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByIdFull() {
    // When
    var result = controller.getSpecimenByIdFull(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByVersion() throws Exception {
    // When
    var result = controller.getSpecimenByVersion(PREFIX, SUFFIX, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    then(service).should().getSpecimenByVersion(eq(DOI + ID), eq(1), anyString());
  }

  @Test
  void testGetSpecimenVersions() throws Exception {
    // When
    var result = controller.getSpecimenVersions(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    then(service).should().getSpecimenVersions(eq(DOI + ID), anyString());

  }

  @Test
  void testGetSpecimenAnnotations() {
    // When
    var result = controller.getSpecimenAnnotations(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMjrsForSpecimen() throws Exception {

    // When
    var result = controller.getMasJobRecordsForSpecimen(PREFIX, SUFFIX, JobState.SCHEDULED,
        1, 1, mockRequest);

    // THen
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenDigitalMedia() {
    // When
    var result = controller.getSpecimenDigitalMedia(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testSearch() throws Exception {
    //Given
    var paramMap = new MultiValueMapAdapter(Map.of("q", List.of("queryString")));
    given(service.search(eq(paramMap), anyString())).willReturn(
        new JsonApiListResponseWrapper(givenDigitalSpecimenJsonApiDataList(2), 1, 2, "test"));

    // When
    var result = controller.search(paramMap, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(((JsonApiListResponseWrapper) result.getBody()).getData()).isEqualTo(
        givenDigitalSpecimenJsonApiDataList(2));
  }

  @Test
  void testAggregation() throws Exception {
    //Given
    var paramMap = new MultiValueMapAdapter(Map.of("SourceSystemId", List.of(SOURCE_SYSTEM_ID_1)));
    var data = new JsonApiData("id", "aggregations", MAPPER.valueToTree(givenAggregationMap()));
    given(service.aggregations(eq(paramMap), anyString())).willReturn(
        new JsonApiWrapper(data, new JsonApiLinks("test")));

    // When
    var result = controller.aggregation(paramMap, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(((JsonApiWrapper) result.getBody()).getData()).isEqualTo(data);
  }

  @Test
  void testTaxonAggregation() throws Exception {
    //Given
    var paramMap = new MultiValueMapAdapter(Map.of("Kingdom", List.of("Animalia")));
    var data = new JsonApiData("id", "aggregations",
        MAPPER.valueToTree(givenTaxonAggregationMap()));
    given(service.taxonAggregations(eq(paramMap), anyString())).willReturn(
        new JsonApiWrapper(data, new JsonApiLinks("test")));

    // When
    var result = controller.taxonAggregation(paramMap, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(((JsonApiWrapper) result.getBody()).getData()).isEqualTo(data);
  }


  @Test
  void testDiscipline() throws Exception {
    //Given
    var data = new JsonApiData("id", "aggregations", MAPPER.valueToTree(givenAggregationMap()));
    given(service.discipline(anyString())).willReturn(
        new JsonApiWrapper(data, new JsonApiLinks("test")));

    // When
    var result = controller.discipline(mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat((result.getBody()).getData()).isEqualTo(data);
  }

  @Test
  void testGetSpecimenByVersionFull() throws Exception {
    // When
    var result = controller.getSpecimenByVersionFull(PREFIX, SUFFIX, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    then(service).should().getSpecimenByVersionFull(eq(DOI + ID), eq(1), anyString());
  }

  @Test
  void testSearchTermValue() throws Exception {
    //Given
    var data = new JsonApiData("id", "aggregations", MAPPER.valueToTree(givenAggregationMap()));
    given(service.searchTermValue(anyString(), anyString(), anyString(), eq(false))).willReturn(
        new JsonApiWrapper(data, new JsonApiLinks("test")));

    // When
    var result = controller.searchTermValue("sourceSystem", "20.500", false, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat((result.getBody()).getData()).isEqualTo(data);
  }

  @Test
  void testGetMas() {
    // Given
    var expectedResponse = givenMasResponse(SPECIMEN_PATH);
    given(service.getMass(ID, SPECIMEN_PATH)).willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.getMassForDigitalSpecimen(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testScheduleMas() throws Exception {
    // Given
    var expectedResponse = givenMasResponse(SPECIMEN_PATH);
    var request = givenMasRequest();
    givenAuthentication();
    given(service.scheduleMass(ID, Map.of(HANDLE + ID, givenMasJobRequest()), ORCID,
        SPECIMEN_PATH)).willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.scheduleMassForDigitalSpecimen(PREFIX, SUFFIX, request, authentication,
        mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testScheduleMasInvalidType() {
    // Given
    var request = givenMasRequest("Invalid Type");
    givenAuthentication();

    // When / Then
    assertThrowsExactly(ConflictException.class,
        () -> controller.scheduleMassForDigitalSpecimen(PREFIX, SUFFIX, request, authentication,
            mockRequest));
  }

  @Test
  void testScheduleMasNoAttribute() throws Exception{
    // Given
    var mass = Map.of("somethingElse", Map.of(ID, false));
    var apiRequest = new JsonApiRequest("MasRequest", MAPPER.valueToTree(mass));
    var request = new JsonApiRequestWrapper(apiRequest);
    givenAuthentication();

    // When / Then
    assertThrowsExactly(IllegalArgumentException.class,
        () -> controller.scheduleMassForDigitalSpecimen(PREFIX, SUFFIX, request, authentication,
            mockRequest));
  }

  @Test
  void testGetOriginalDataForSpecimenForSpecimen(){
    // When
    var result = controller.getOriginalDataForSpecimen(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void givenAuthentication() {
    var principal = mock(Jwt.class);
    given(authentication.getPrincipal()).willReturn(principal);
    given(principal.getClaims()).willReturn(givenClaims());
  }

}
