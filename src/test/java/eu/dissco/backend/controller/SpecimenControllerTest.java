package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.givenAggregationMap;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_URI;
import static eu.dissco.backend.utils.SpecimenUtils.givenDigitalSpecimenJsonApiDataList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.service.SpecimenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.MultiValueMapAdapter;

@ExtendWith(MockitoExtension.class)
class SpecimenControllerTest {

  MockHttpServletRequest mockRequest;
  @Mock
  private SpecimenService service;
  private SpecimenController controller;

  @BeforeEach
  void setup() {
    controller = new SpecimenController(service);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(SPECIMEN_URI);
  }

  @Test
  void testGetSpecimen() {
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
  void testGetSpecimenByIdJsonLd() {
    // When
    var result = controller.getSpecimenByIdJsonLD(PREFIX, SUFFIX);

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
  }

  @Test
  void testGetSpecimenVersions() throws Exception {
    // When
    var result = controller.getSpecimenVersions(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenAnnotations() {
    // When
    var result = controller.getSpecimenAnnotations(PREFIX, SUFFIX, mockRequest);

    // Then
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
  void testDiscipline() throws Exception {
    //Given
    var data = new JsonApiData("id", "aggregations", MAPPER.valueToTree(givenAggregationMap()));
    given(service.discipline(anyString())).willReturn(new JsonApiWrapper(data, new JsonApiLinks("test")));

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
  }

  @Test
  void testSearchTermValue() throws Exception {
    //Given
    var data = new JsonApiData("id", "aggregations", MAPPER.valueToTree(givenAggregationMap()));
    given(service.searchTermValue(anyString(), anyString(), anyString())).willReturn(new JsonApiWrapper(data, new JsonApiLinks("test")));

    // When
    var result = controller.searchTermValue("sourceSystem", "20.500", mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat((result.getBody()).getData()).isEqualTo(data);
  }

}
