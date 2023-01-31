package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.POSTFIX;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static eu.dissco.backend.TestUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.TestUtils.givenDigitalMediaJsonResponse;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenJsonApiLinksFull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalMediaObjectServiceTest {

  @Mock
  private DigitalMediaObjectRepository repository;
  @Mock
  private  AnnotationService annotationService;

  private DigitalMediaObjectService service;

  @BeforeEach
  void setup(){
    service = new DigitalMediaObjectService(repository, annotationService);
  }

  @Test
  void testGetDigitalMediaById(){
    // Given
    var responseExpected = givenDigitalMediaObject(ID);
    given(repository.getLatestDigitalMediaById(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaById(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaByIJsonResponse(){
    // Given
    String path = SANDBOX_URI + "/json/" + ID;
    var dataNode = givenDigitalMediaJsonApiData(ID);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    given(repository.getLatestDigitalMediaObjectByIdJsonResponse(ID)).willReturn(dataNode);

    // When
    var responseReceived = service.getDigitalMediaByIdJsonResponse(ID, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaByIdJsonResponse(){
    // Given
    List<String> mediaIds = List.of("1", "2", "3");
    List<DigitalMediaObject> mediaObjects = new ArrayList<>();
    List<DigitalMediaObjectFull> responseExpected = new ArrayList<>();

    for (String id: mediaIds){
      var mediaObject = givenDigitalMediaObject(id);
      mediaObjects.add(mediaObject);
      var annotation = givenAnnotationResponse();
      responseExpected.add(new DigitalMediaObjectFull(mediaObject, List.of(annotation)));
      given(annotationService.getAnnotationForTarget(String.valueOf(id))).willReturn(List.of(annotation));
    }
    given(repository.getForDigitalSpecimen(ID)).willReturn(mediaObjects);

    // When
    var responseReceived = service.getDigitalMediaObjectFull(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersions(){
    // Given
    List<Integer> responseExpected = List.of(1, 2);
    given(repository.getDigitalMediaVersions(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaVersions(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersion(){
    // Given
    int version = 1;
    var responseExpected = givenDigitalMediaObject(ID);
    given(repository.getDigitalMediaByVersion(ID, version)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaVersion(ID, version);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersionJsonResponse(){
    // Given
    int version = 1;
    String path = SANDBOX_URI + "/json/" + ID + "/" + String.valueOf(version);
    var dataNode = givenDigitalMediaJsonApiData(ID);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    given(repository.getDigitalMediaByVersionJsonResponse(ID, version)).willReturn(dataNode);

    // When
    var responseReceived = service.getDigitalMediaVersionJsonResponse(ID, version, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaForSpecimen(){
    // Given
    var responseExpected = List.of(givenDigitalMediaObject(ID));
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaForSpecimen(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaObjects(){
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var responseExpected = Collections.nCopies(pageSize, givenDigitalMediaObject(ID));
    given(repository.getDigitalMediaObject(pageNumber, pageSize)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaObjectsJsonResponse(){
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    int totalPageCount = 10;
    String path = SANDBOX_URI + "json";

    var dataNode = Collections.nCopies(pageSize, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, totalPageCount);
    var metaNode = new JsonApiMeta(totalPageCount);

    given(repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize)).willReturn(dataNode);
    given(repository.getMediaObjectCount(pageSize)).willReturn(totalPageCount);

    var responseExpected = new JsonApiMetaWrapper(dataNode, linksNode, metaNode);

    // When
    var responseReceived = service.getDigitalMediaObjectsJsonResponse(pageNumber, pageSize, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaIdsForSpecimen(){
    // Given
    List<String> responseExpected = List.of("1, 2");
    given(repository.getDigitalMediaIdsForSpecimen(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaIdsForSpecimen(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }
}
