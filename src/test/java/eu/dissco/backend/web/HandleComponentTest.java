package eu.dissco.backend.web;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.HandleUtils.givenPostHandleResponse;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionHandleRollbackRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionTombstoneHandleRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionUpdateHandleRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.client.HandleClient;
import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.exceptions.PidException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class HandleComponentTest {

  @Mock
  private FdoRecordComponent fdoRecordComponent;
  @Mock
  private HandleClient handleClient;
  private HandleComponent handleComponent;


  @BeforeEach
  void setup() {
    handleComponent = new HandleComponent(handleClient, fdoRecordComponent);
  }

  @Test
  void testPostHandleMjr() throws Exception {
    // Given
    int n = 1;
    var responseBody = givenPostHandleResponse(n);
    var expected = List.of(ID);
    given(handleClient.postHandles(any())).willReturn(responseBody);

    // When
    var response = handleComponent.postHandleMjr(n);

    // Then
    assertThat(response).isEqualTo(expected);
  }

  @Test
  void testPostHandleVirtualCollection() throws Exception {
    // Given
    var clientResponse = givenPostHandleResponse(1);
    var virtualCollectionRequest = givenVirtualCollectionRequest();
    given(handleClient.postHandle(any())).willReturn(clientResponse);

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

    // When
    handleComponent.rollbackVirtualCollection(HANDLE + ID);

    // Then
    then(handleClient).should().rollbackHandle(givenVirtualCollectionHandleRollbackRequest());
  }

  @Test
  void testDataNodeNotReadable() {
    // Given
    var responseBody = MAPPER.createObjectNode();
    responseBody.set("data", MAPPER.createArrayNode());

    // When / Then
    assertThrows(PidException.class, () -> handleComponent.postHandleMjr(1));
  }

  @Test
  void testTombstoneHandle() throws Exception {
    // Given
    var request = givenVirtualCollectionTombstoneHandleRequest();
    given(fdoRecordComponent.getTombstoneRequest(ID)).willReturn(request);

    // When
    handleComponent.tombstoneHandle(ID);

    // Then
    then(handleClient).should().tombstoneHandle(ID, request);
  }

  @Test
  void testUpdateHandle() throws Exception {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    given(fdoRecordComponent.getPatchHandleRequest(virtualCollection)).willReturn(givenVirtualCollectionUpdateHandleRequest());

    // When
    handleComponent.updateHandle(virtualCollection);

    // Then
    then(handleClient).should().updateHandle(givenVirtualCollectionUpdateHandleRequest());
  }
}
