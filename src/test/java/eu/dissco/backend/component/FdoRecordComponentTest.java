package eu.dissco.backend.component;


import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.HandleUtils.givenPostHandleRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionHandleRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionHandleRollbackRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionRequest;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.utils.VirtualCollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FdoRecordComponentTest {

  private FdoRecordComponent fdoRecordComponent;

  @BeforeEach
  void setup() {
    fdoRecordComponent = new FdoRecordComponent(MAPPER);
  }

  @Test
  void testGetRequest() throws Exception {
    // Given

    // When
    var result = fdoRecordComponent.getPostRequest();

    // Then
    assertThat(result).isEqualTo(givenPostHandleRequest());
  }

  @Test
  void testGetPostRequest() {
    // Given
    var virtualCollectionHandleRequest = givenVirtualCollectionRequest();

    // When
    var result = fdoRecordComponent.getPostRequest(virtualCollectionHandleRequest);

    // Then
    assertThat(result).isEqualTo(givenVirtualCollectionHandleRequest());
  }

  @Test
  void testGetRollbackCreateRequest() {
    // Given
    var handle = HANDLE + ID;

    // When
    var result = fdoRecordComponent.getRollbackCreateRequest(handle);

    // Then
    assertThat(result).isEqualTo(givenVirtualCollectionHandleRollbackRequest());
  }

}
