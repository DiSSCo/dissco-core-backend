package eu.dissco.backend.web;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.HandleUtils.givenPostHandleResponse;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionHandleRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionHandleRollbackRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionTombstoneHandleRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionUpdateHandleRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.exceptions.PidException;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class HandleComponentTest {

  private static MockWebServer mockHandleServer;
  @Mock
  private TokenAuthenticator tokenAuthenticator;
  @Mock
  private FdoRecordComponent fdoRecordComponent;
  private HandleComponent handleComponent;

  @BeforeAll
  static void init() throws IOException {
    mockHandleServer = new MockWebServer();
    mockHandleServer.start();
  }

  @AfterAll
  static void destroy() throws IOException {
    mockHandleServer.shutdown();
  }

  @BeforeEach
  void setup() {
    WebClient webClient = WebClient.create(
        String.format("http://%s:%s", mockHandleServer.getHostName(), mockHandleServer.getPort()));
    handleComponent = new HandleComponent(webClient, tokenAuthenticator, fdoRecordComponent);
  }

  @Test
  void testPostHandleMjr() throws Exception {
    // Given
    int n = 1;
    var responseBody = givenPostHandleResponse(n);
    var expected = List.of(ID);
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(responseBody))
        .addHeader("Content-Type", "application/json"));

    // When
    var response = handleComponent.postHandleMjr(n);

    // Then
    assertThat(response).isEqualTo(expected);
  }

  @Test
  void testPostHandleVirtualCollection() throws Exception {
    // Given
    var responseBody = givenPostHandleResponse(1);
    var virtualCollectionRequest = givenVirtualCollectionRequest();
    given(fdoRecordComponent.getPostRequestVirtualCollection(virtualCollectionRequest)).willReturn(
        givenVirtualCollectionHandleRequest());
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(responseBody))
        .addHeader("Content-Type", "application/json"));

    // When
    var response = handleComponent.postHandleVirtualCollection(virtualCollectionRequest);

    // Then
    assertThat(response).isEqualTo(ID);
  }

  @Test
  void testRollbackHandleVirtualCollection() throws PidException {
    // Given
    given(fdoRecordComponent.getRollbackCreateRequest(HANDLE + ID)).willReturn(
        givenVirtualCollectionHandleRollbackRequest());
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

    // When
    handleComponent.rollbackVirtualCollection(HANDLE + ID);

    // Then
    then(fdoRecordComponent).should().getRollbackCreateRequest(HANDLE + ID);
  }

  @Test
  void testUnauthorized() {
    // Given

    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.UNAUTHORIZED.value())
        .addHeader("Content-Type", "application/json"));

    // Then
    assertThrows(PidException.class, () -> handleComponent.postHandleMjr(1));
  }

  @Test
  void testBadRequest() {
    // Given
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value())
        .addHeader("Content-Type", "application/json"));

    // Then
    assertThrows(PidException.class, () -> handleComponent.postHandleMjr(1));
  }

  @Test
  void testRetriesFail() {
    // Given
    int requestCount = mockHandleServer.getRequestCount();

    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));

    // Then
    assertThrows(PidException.class, () -> handleComponent.postHandleMjr(1));
    assertThat(mockHandleServer.getRequestCount() - requestCount).isEqualTo(4);
  }

  @Test
  void testInterruptedException() throws Exception {
    // Given
    var responseBody = givenPostHandleResponse(1);
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(responseBody))
        .addHeader("Content-Type", "application/json"));

    Thread.currentThread().interrupt();

    // When
    var response = assertThrows(PidException.class,
        () -> handleComponent.postHandleMjr(1));

    // Then
    assertThat(response).hasMessage(
        "Interrupted execution: A connection error has occurred in creating a jobId.");
  }

  @Test
  void testDataNodeNotArray() throws Exception {
    // Given
    var responseBody = MAPPER.createObjectNode();
    responseBody.put("data", "val");
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(responseBody))
        .addHeader("Content-Type", "application/json"));
    // Then
    assertThrows(PidException.class, () -> handleComponent.postHandleMjr(1));
  }

  @Test
  void testTombstoneHandle() throws Exception {
    // Given
    var request = givenVirtualCollectionTombstoneHandleRequest();
    given(fdoRecordComponent.getTombstoneRequest(ID)).willReturn(request);
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(request))
        .addHeader("Content-Type", "application/json"));

    // When
    handleComponent.tombstoneHandle(ID);

    // Then
    then(fdoRecordComponent).should().getTombstoneRequest(ID);
  }

  @Test
  void testUpdateHandle() throws Exception {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    given(fdoRecordComponent.getPatchHandleRequest(virtualCollection)).willReturn(givenVirtualCollectionUpdateHandleRequest());
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(virtualCollection))
        .addHeader("Content-Type", "application/json"));

    // When
    handleComponent.updateHandle(virtualCollection);

    // Then
    then(fdoRecordComponent).should().getPatchHandleRequest(virtualCollection);
  }
}
