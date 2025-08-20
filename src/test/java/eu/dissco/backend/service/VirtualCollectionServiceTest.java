package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.givenCreator;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_PATH;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionJsonResponse;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionResponseList;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionResponseWrapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.VirtualCollectionRepository;
import eu.dissco.backend.utils.VirtualCollectionUtils;
import eu.dissco.backend.web.HandleComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VirtualCollectionServiceTest {

  @Mock
  private VirtualCollectionRepository repository;
  @Mock
  private MongoRepository mongoRepository;
  @Mock
  private HandleComponent handleComponent;
  @Mock
  private RabbitMqPublisherService rabbitMqPublisherService;

  private MockedStatic<Instant> mockedInstant;
  private MockedStatic<Clock> mockedClock;
  private VirtualCollectionService service;

  @BeforeEach
  void setup() {
    service = new VirtualCollectionService(repository, rabbitMqPublisherService, mongoRepository,
        handleComponent, MAPPER);
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    mockedInstant = mockStatic(Instant.class);
    mockedInstant.when(Instant::now).thenReturn(instant);
    mockedInstant.when(() -> Instant.from(any())).thenReturn(instant);
    mockedInstant.when(() -> Instant.parse(any())).thenReturn(instant);
    mockedClock = mockStatic(Clock.class);
    mockedClock.when(Clock::systemUTC).thenReturn(clock);
  }

  @AfterEach
  void destroy() {
    mockedInstant.close();
    mockedClock.close();
  }

  @Test
  void testGetVirtualCollectionById() throws NotFoundException {
    // Given
    var expected = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH);
    given(repository.getVirtualCollectionById(ID))
        .willReturn(VirtualCollectionUtils.givenVirtualCollection(HANDLE + ID));

    // When
    var result = service.getVirtualCollectionById(ID, VIRTUAL_COLLECTION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVirtualCollectionNotFound() {
    // Given

    // When / Then
    assertThrows(NotFoundException.class,
        () -> service.getVirtualCollectionById(ID, VIRTUAL_COLLECTION_PATH));
  }

  @Test
  void testGetVirtualCollections() {
    // Given
    var expected = givenVirtualCollectionJsonResponse(VIRTUAL_COLLECTION_PATH, 1, 15, ORCID, ID,
        true);
    given(repository.getVirtualCollections(1, 15)).willReturn(
        givenVirtualCollectionResponseList(ID, 115));

    // When
    var result = service.getVirtualCollections(1, 15, VIRTUAL_COLLECTION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVirtualCollectionsForUser() {
    // Given
    var expected = givenVirtualCollectionJsonResponse(VIRTUAL_COLLECTION_PATH, 1, 15, ORCID, ID,
        false);
    given(repository.getVirtualCollectionsForUser(ORCID, 1, 15)).willReturn(
        givenVirtualCollectionResponseList(ID, 15));

    // When
    var result = service.getVirtualCollectionsForUser(ORCID, 1, 15, VIRTUAL_COLLECTION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVirtualCollectionByVersion() throws NotFoundException, JsonProcessingException {
    // Given
    var version = 2;
    var virtualCollectionNode = MAPPER.valueToTree(
        VirtualCollectionUtils.givenVirtualCollection(HANDLE + ID));
    var expected = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH);
    given(mongoRepository.getByVersion(ID, version, "virtual_collection_provenance"))
        .willReturn(virtualCollectionNode);

    // When
    var result = service.getVirtualCollectionByVersion(ID, version, VIRTUAL_COLLECTION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVirtualCollectionVersions() throws NotFoundException {
    // Given
    List<Integer> versionsList = List.of(1, 2);
    var versionsNode = MAPPER.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    arrayNode.add(1).add(2);
    var dataNode = new JsonApiData(ID, "virtualCollectionVersions", versionsNode);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(VIRTUAL_COLLECTION_PATH));

    given(mongoRepository.getVersions(ID, "virtual_collection_provenance")).willReturn(
        versionsList);
    try (var mockedStatic = mockStatic(DigitalServiceUtils.class)) {
      mockedStatic.when(() -> DigitalServiceUtils.createVersionNode(versionsList, MAPPER))
          .thenReturn(versionsNode);
      // When
      var responseReceived = service.getVirtualCollectionVersions(ID, VIRTUAL_COLLECTION_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
  }

  @Test
  void persistVirtualCollection() throws JsonProcessingException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var request = givenVirtualCollectionRequest();
    var expected = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH, ORCID);
    given(handleComponent.postHandleVirtualCollection(request)).willReturn(ID);

    // When
    var result = service.persistVirtualCollection(request, givenCreator(ORCID),
        VIRTUAL_COLLECTION_PATH);

    // Then
    then(repository).should().createVirtualCollection(virtualCollection);
    then(rabbitMqPublisherService).should()
        .publishCreateEvent(MAPPER.valueToTree(virtualCollection), givenCreator(ORCID));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void persistVirtualCollectionRollback() throws JsonProcessingException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var request = givenVirtualCollectionRequest();
    var creator = givenCreator(ORCID);
    given(handleComponent.postHandleVirtualCollection(request)).willReturn(ID);
    doThrow(new JsonParseException("Failed to parse")).when(rabbitMqPublisherService)
        .publishCreateEvent(MAPPER.valueToTree(virtualCollection), creator);

    // When
    assertThrows(
        ProcessingFailedException.class,
        () -> service.persistVirtualCollection(request, creator, VIRTUAL_COLLECTION_PATH));

    // Then
    then(repository).should().createVirtualCollection(virtualCollection);
    then(handleComponent).should().rollbackVirtualCollection(HANDLE + ID);
    then(repository).should().rollbackVirtualCollectionCreate(HANDLE + ID);
  }


}
