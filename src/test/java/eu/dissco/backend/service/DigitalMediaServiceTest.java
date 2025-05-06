package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.TestUtils.givenJsonApiLinksFull;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.DigitalMediaFull;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalMediaRepository;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.schema.DigitalMedia;
import eu.dissco.backend.schema.EntityRelationship;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalMediaServiceTest {

  @Mock
  private DigitalMediaRepository repository;
  @Mock
  private AnnotationService annotationService;
  @Mock
  private MachineAnnotationServiceService masService;
  @Mock
  private MongoRepository mongoRepository;
  @Mock
  private DigitalSpecimenRepository digitalSpecimenRepository;
  @Mock
  private MasJobRecordService masJobRecordService;

  private DigitalMediaService service;

  static Stream<List<EntityRelationship>> missingSpecimenDoiAttributes() {
    return Stream.of(List.of(), List.of(
        new EntityRelationship().withDwcRelatedResourceID(SOURCE_SYSTEM_ID_1)
            .withDwcRelationshipOfResource("hasSourceSystem")));
  }

  @BeforeEach
  void setup() {
    service = new DigitalMediaService(repository, annotationService, digitalSpecimenRepository,
        masService, mongoRepository, MAPPER, masJobRecordService);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetDigitalMediaObjects(int pageNumber) {
    // Given
    int pageSize = 10;

    var repositoryResponse = Collections.nCopies(pageSize + 1, givenDigitalMediaObject(ID));
    var dataNodePlusOne = Collections.nCopies(pageSize + 1, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(DIGITAL_MEDIA_PATH, pageNumber, pageSize, true);
    var dataNode = dataNodePlusOne.subList(0, pageSize);
    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);
    given(repository.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(repositoryResponse);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaObjectsLastPage() {
    // Given
    int pageSize = 10;
    int pageNumber = 1;

    var repositoryResponse = Collections.nCopies(pageSize, givenDigitalMediaObject(ID));
    var dataNode = Collections.nCopies(pageSize, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(DIGITAL_MEDIA_PATH, pageNumber, pageSize, false);

    given(repository.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(repositoryResponse);

    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaById() {
    // Given
    var mediaObject = givenDigitalMediaObject(ID);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(mediaObject);
    var expected = new JsonApiWrapper(
        new JsonApiData(mediaObject.getDctermsIdentifier(), FdoType.DIGITAL_MEDIA.getName(),
            MAPPER.valueToTree(mediaObject)), new JsonApiLinks(DIGITAL_MEDIA_PATH));

    // When
    var responseReceived = service.getDigitalMediaById(ID, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsOnDigitalMedia() {
    // Given
    var expected = givenDigitalMediaJsonResponse(ANNOTATION_PATH, 1, 1, List.of(ID));
    given(annotationService.getAnnotationForTarget(ID, ANNOTATION_PATH)).willReturn(expected);

    //
    var result = service.getAnnotationsOnDigitalMedia(ID, ANNOTATION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // Given
    List<Integer> versionsList = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "digital_media_provenance")).willReturn(versionsList);
    var versionsNode = MAPPER.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    arrayNode.add(1).add(2);
    var dataNode = new JsonApiData(ID, "digitalMediaVersions", versionsNode);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(DIGITAL_MEDIA_PATH));
    try (var mockedStatic = mockStatic(DigitalServiceUtils.class)) {
      mockedStatic.when(() -> DigitalServiceUtils.createVersionNode(versionsList, MAPPER))
          .thenReturn(versionsNode);

      // When
      var responseReceived = service.getDigitalMediaVersions(ID, DIGITAL_MEDIA_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
  }

  @Test
  void testGetDigitalMediaByVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    var mongoResponse = givenMongoDBMediaResponse();
    given(mongoRepository.getByVersion(ID, version, "digital_media_provenance")).willReturn(
        mongoResponse);

    var expectedResponse = new JsonApiWrapper(
        new JsonApiData(DOI + ID, FdoType.DIGITAL_MEDIA.getName(),
            MAPPER.valueToTree(givenDigitalMediaObject(DOI + ID))),
        new JsonApiLinks(DIGITAL_MEDIA_PATH));

    // When
    var responseReceived = service.getDigitalMediaObjectByVersion(ID, version, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expectedResponse);
  }

  @Test
  void testGetFullDigitalMediaFromSpecimen() {
    // Given
    var firstId = "1";
    var secondId = "2";
    List<String> mediaIds = List.of(firstId, secondId);
    List<DigitalMedia> mediaObjects = List.of(givenDigitalMediaObject(firstId),
        givenDigitalMediaObject(secondId));
    List<DigitalMediaFull> expected = List.of(
        new DigitalMediaFull(givenDigitalMediaObject(firstId),
            List.of(givenAnnotationResponse(ID + firstId, ORCID, firstId))),
        new DigitalMediaFull(givenDigitalMediaObject(secondId),
            List.of(givenAnnotationResponse(ID + secondId, ORCID, secondId))));
    var specimen = givenDigitalSpecimenWrapper(ID)
        .withOdsHasEntityRelationships(
            List.of(
                new EntityRelationship()
                    .withDwcRelationshipOfResource("hasDigitalMedia")
                    .withDwcRelatedResourceID(firstId),
                new EntityRelationship()
                    .withDwcRelationshipOfResource("hasDigitalMedia")
                    .withDwcRelatedResourceID(secondId))
        );
    given(annotationService.getAnnotationForTargetObjects(mediaIds)).willReturn(Map.of(
        firstId, List.of(givenAnnotationResponse(ID + firstId, ORCID, firstId)),
        secondId, List.of(givenAnnotationResponse(ID + secondId, ORCID, secondId))
    ));
    given(repository.getLatestDigitalMediaObjectsById(List.of(firstId, secondId))).willReturn(mediaObjects);

    // When
    var received = service.getFullDigitalMediaFromSpecimen(specimen);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetMas() {
    // Given
    var digitalMedia = givenDigitalMediaObject(HANDLE + ID);
    var specimenId = DOI + ID_ALT;
    var digitalSpecimen = givenDigitalSpecimenWrapper(specimenId);
    var response = givenMasResponse(DIGITAL_MEDIA_PATH);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMedia);
    given(digitalSpecimenRepository.getLatestSpecimenById(ID_ALT)).willReturn(digitalSpecimen);
    given(masService.getMassForObject(any(JsonNode.class), eq(DIGITAL_MEDIA_PATH))).willReturn(
        response);

    // When
    var result = service.getMass(ID, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  @ParameterizedTest
  @MethodSource("missingSpecimenDoiAttributes")
  void testGetMasWithoutSpecimenId(List<EntityRelationship> entityRelationships) {
    // Given
    var digitalMedia = givenDigitalMediaObject(HANDLE + ID);
    digitalMedia.setOdsHasEntityRelationships(entityRelationships);
    var response = givenMasResponse(DIGITAL_MEDIA_PATH);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMedia);
    given(digitalSpecimenRepository.getLatestSpecimenById(null)).willReturn(null);
    given(masService.getMassForObject(any(JsonNode.class), eq(DIGITAL_MEDIA_PATH))).willReturn(
        response);

    // When
    var result = service.getMass(ID, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  @Test
  void testScheduleMas() throws Exception {
    // Given
    var digitalMediaWrapper = givenDigitalMediaObject(HANDLE + ID);
    var specimenId = DOI + ID_ALT;
    var digitalSpecimen = givenDigitalSpecimenWrapper(specimenId);
    var response = givenMasResponse(DIGITAL_MEDIA_PATH);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMediaWrapper);
    given(digitalSpecimenRepository.getLatestSpecimenById(ID_ALT)).willReturn(digitalSpecimen);
    given(masService.scheduleMass(any(JsonNode.class), eq(Map.of(ID, givenMasJobRequest())),
        eq(DIGITAL_MEDIA_PATH),
        eq(digitalMediaWrapper), eq(ID), eq(ORCID), eq(MjrTargetType.MEDIA_OBJECT))).willReturn(
        response);

    // When
    var result = service.scheduleMass(ID, Map.of(ID, givenMasJobRequest()), DIGITAL_MEDIA_PATH,
        ORCID);

    // Then
    assertThat(result).isEqualTo(response);
  }


  @Test
  void testScheduleMasMediaNotFound() {
    // Given
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(null);

    // When / Then
    assertThrows(NotFoundException.class,
        () -> service.scheduleMass(ID, Map.of(ID, givenMasJobRequest()), DIGITAL_MEDIA_PATH,
            ORCID));
  }

  @Test
  void testScheduleMasSpecimenNotFound() {
    // Given
    var digitalMediaWrapper = givenDigitalMediaObject(HANDLE + ID);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMediaWrapper);
    given(digitalSpecimenRepository.getLatestSpecimenById(ID_ALT)).willReturn(null);

    // When / Then
    assertThrows(NotFoundException.class,
        () -> service.scheduleMass(ID, Map.of(ID, givenMasJobRequest()), DIGITAL_MEDIA_PATH,
            ORCID));
  }

  @Test
  void testGetOriginalDataForMedia() throws JsonProcessingException {
    // Given
    var expectedJson = givenMongoDBMediaResponse();
    var expected = new JsonApiWrapper(
        new JsonApiData(ID, FdoType.DIGITAL_MEDIA.getName(), expectedJson),
        new JsonApiLinks(SANDBOX_URI));
    given(repository.getMediaOriginalData(ID)).willReturn(expectedJson);

    // When
    var result = service.getOriginalDataForMedia(ID, SANDBOX_URI);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  private JsonNode givenMongoDBMediaResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """ 
            {
                   "@id": "https://doi.org/20.5000.1025/ABC-123-XYZ",
                   "@type": "ods:DigitalMedia",
                   "dcterms:identifier": "https://doi.org/20.5000.1025/ABC-123-XYZ",
                   "ods:version": 1,
                   "ods:status": "Active",
                   "dcterms:created": "2022-11-01T09:59:24.000Z",
                   "ods:fdoType": "https://doi.org/21.T11148/bbad8c4e101e8af01115",
                   "dcterms:type": "StillImage",
                   "ac:accessURI": "https://dissco.com",
                   "dcterms:format":"image/jpeg",
                   "ods:sourceSystemID": "https://hdl.handle.net/20.5000.1025/3XA-8PT-SAY",
                   "ods:hasEntityRelationships": [
                     {
                       "@type": "ods:EntityRelationship",
                       "dwc:relationshipOfResource": "hasDigitalSpecimen",
                       "dwc:relatedResourceID": "https://doi.org/20.5000.1025/AAA-111-ZZZ"
                     }
                   ],
                   "ods:hasAgents": []
                 }
            """, JsonNode.class
    );
  }
}
