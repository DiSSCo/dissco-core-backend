package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.givenAgent;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_NAME;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_PATH;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTargetFilter;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTombstoneVirtualCollection;
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
import eu.dissco.backend.exceptions.PidException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.VirtualCollectionRepository;
import eu.dissco.backend.schema.OdsHasPredicate;
import eu.dissco.backend.schema.OdsHasPredicate.OdsPredicateType;
import eu.dissco.backend.schema.TargetDigitalObjectFilter;
import eu.dissco.backend.schema.VirtualCollectionRequest.LtcBasisOfScheme;
import eu.dissco.backend.utils.VirtualCollectionUtils;
import eu.dissco.backend.web.HandleComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
  void persistVirtualCollection() throws JsonProcessingException, PidException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var request = givenVirtualCollectionRequest();
    var expected = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH, ORCID);
    given(handleComponent.postHandleVirtualCollection(request)).willReturn(ID);

    // When
    var result = service.persistVirtualCollection(request, givenAgent(),
        VIRTUAL_COLLECTION_PATH);

    // Then
    then(repository).should().createVirtualCollection(virtualCollection);
    then(rabbitMqPublisherService).should()
        .publishCreateEvent(MAPPER.valueToTree(virtualCollection), givenAgent());
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void persistVirtualCollectionRollback() throws JsonProcessingException, PidException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var request = givenVirtualCollectionRequest();
    var creator = givenAgent();
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

  @Test
  void testTombstoneVirtualCollection()
      throws PidException, JsonProcessingException, NotFoundException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    var tombstoneVirtualCollection = givenTombstoneVirtualCollection();
    given(repository.getActiveVirtualCollection(ID, null)).willReturn(
        Optional.of(virtualCollection));

    // When
    var result = service.tombstoneVirtualCollection(PREFIX, SUFFIX, givenAgent(), true);

    // Then
    then(handleComponent).should().tombstoneHandle(ID);
    then(repository).should().tombstoneVirtualCollection(tombstoneVirtualCollection);
    then(rabbitMqPublisherService).should()
        .publishTombstoneEvent(MAPPER.valueToTree(tombstoneVirtualCollection),
            MAPPER.valueToTree(virtualCollection), givenAgent());
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {ORCID})
  @NullSource
  void testTombstoneVirtualCollectionNoRecord(String userId) {
    // Given
    given(repository.getActiveVirtualCollection(ID, userId)).willReturn(Optional.empty());

    // When
    assertThrows(NotFoundException.class,
        () -> service.tombstoneVirtualCollection(PREFIX, SUFFIX, givenAgent(), userId == null));

    // Then
    then(handleComponent).shouldHaveNoInteractions();
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
  }

  @Test
  void testTombstoneVirtualCollectionHandleException()
      throws PidException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    given(repository.getActiveVirtualCollection(ID, null)).willReturn(
        Optional.of(virtualCollection));
    doThrow(new PidException("Handle tombstoning failed")).when(handleComponent)
        .tombstoneHandle(ID);
    var agent = givenAgent();

    // When
    assertThrows(ProcessingFailedException.class,
        () -> service.tombstoneVirtualCollection(PREFIX, SUFFIX, agent, true));

    // Then
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
  }

  @Test
  void testTombstoneVirtualCollectionRabbitException()
      throws PidException, JsonProcessingException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    var agent = givenAgent();
    var tombstoneVirtualCollection = givenTombstoneVirtualCollection();
    given(repository.getActiveVirtualCollection(ID, null)).willReturn(
        Optional.of(virtualCollection));
    doThrow(new JsonParseException("Handle tombstoning failed")).when(rabbitMqPublisherService)
        .publishTombstoneEvent(MAPPER.valueToTree(tombstoneVirtualCollection),
            MAPPER.valueToTree(virtualCollection), givenAgent());

    // When
    assertThrows(ProcessingFailedException.class,
        () -> service.tombstoneVirtualCollection(PREFIX, SUFFIX, agent, true));

    // Then
    then(handleComponent).should().tombstoneHandle(ID);
    then(repository).should().tombstoneVirtualCollection(tombstoneVirtualCollection);
  }

  @Test
  void testUpdateVirtualCollection()
      throws JsonProcessingException, PidException, NotFoundException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var virtualCollectionRequest = givenVirtualCollectionRequest("Updated Name",
        LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter());
    var updatedVirtualCollection = givenVirtualCollection(HANDLE + ID, ORCID, "Updated Name", 2);
    var expected = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH,
        updatedVirtualCollection);
    given(repository.getActiveVirtualCollection(ID, ORCID)).willReturn(
        Optional.of(virtualCollection));

    // When
    var result = service.updateVirtualCollection(ID, virtualCollectionRequest, givenAgent(),
        VIRTUAL_COLLECTION_PATH);

    // Then
    then(handleComponent).should().updateHandle(updatedVirtualCollection);
    then(repository).should().updateVirtualCollection(updatedVirtualCollection);
    then(rabbitMqPublisherService).should()
        .publishUpdateEvent(MAPPER.valueToTree(updatedVirtualCollection),
            MAPPER.valueToTree(virtualCollection), givenAgent());
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testUpdateVirtualCollectionNotFound() {
    // Given
    var virtualCollectionRequest = givenVirtualCollectionRequest("Updated Name",
        LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter());
    given(repository.getActiveVirtualCollection(ID, ORCID)).willReturn(
        Optional.empty());

    // When
    assertThrows(NotFoundException.class,
        () -> service.updateVirtualCollection(ID, virtualCollectionRequest, givenAgent(),
            VIRTUAL_COLLECTION_PATH));

    // Then
    then(handleComponent).shouldHaveNoInteractions();
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
  }

  @Test
  void testUpdateVirtualCollectionEqual() throws JsonProcessingException, NotFoundException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    var virtualCollectionRequest = givenVirtualCollectionRequest();
    given(repository.getActiveVirtualCollection(ID, ORCID)).willReturn(
        Optional.of(virtualCollection));

    // When
    var result = service.updateVirtualCollection(ID, virtualCollectionRequest, givenAgent(),
        VIRTUAL_COLLECTION_PATH);

    // Then
    then(handleComponent).shouldHaveNoInteractions();
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
    assertThat(result).isNull();
  }

  @Test
  void testUpdateVirtualCollectionTargetFilters() {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var virtualCollectionRequest = givenVirtualCollectionRequest(VIRTUAL_COLLECTION_NAME,
        LtcBasisOfScheme.REFERENCE_COLLECTION, givenUpdatedTargetFilter());
    given(repository.getActiveVirtualCollection(ID, ORCID)).willReturn(
        Optional.of(virtualCollection));
    var agent = givenAgent();

    // When
    assertThrows(ProcessingFailedException.class,
        () -> service.updateVirtualCollection(ID, virtualCollectionRequest, agent,
            VIRTUAL_COLLECTION_PATH));

    // Then
    then(handleComponent).shouldHaveNoInteractions();
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
  }

  @Test
  void testUpdateVirtualCollectionHandleFails() throws PidException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID);
    var virtualCollectionRequest = givenVirtualCollectionRequest("Updated Name",
        LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter());
    var updatedVirtualCollection = givenVirtualCollection(HANDLE + ID, ORCID, "Updated Name", 2);
    given(repository.getActiveVirtualCollection(ID, ORCID)).willReturn(
        Optional.of(virtualCollection));
    doThrow(new PidException("Handle tombstoning failed")).when(handleComponent)
        .updateHandle(updatedVirtualCollection);
    var agent = givenAgent();

    // When
    assertThrows(ProcessingFailedException.class,
        () -> service.updateVirtualCollection(ID, virtualCollectionRequest, agent,
            VIRTUAL_COLLECTION_PATH));

    // Then
    then(repository).shouldHaveNoMoreInteractions();
    then(rabbitMqPublisherService).shouldHaveNoInteractions();
  }

  private TargetDigitalObjectFilter givenUpdatedTargetFilter() {
    return new TargetDigitalObjectFilter()
        .withOdsHasPredicates(List.of(new OdsHasPredicate()
            .withOdsPredicateType(OdsPredicateType.EQUALS)
            .withOdsPredicateKey("$['ods:topicDiscipline']")
            .withOdsPredicateValue("botany")));
  }


}
