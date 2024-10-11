package eu.dissco.backend.component;


import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.HandleUtils.givenPostHandleRequest;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FdoRecordComponentTest {

  @Test
  void testGetRequest() throws Exception {
    // Given
    var fdoComponent = new FdoRecordComponent(MAPPER);

    // When
    var result = fdoComponent.getPostRequest();

    // Then
    assertThat(result).isEqualTo(givenPostHandleRequest());
  }

}
