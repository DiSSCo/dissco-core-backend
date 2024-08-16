package eu.dissco.backend.component;


import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.HandleUtils.givenPostHandleRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.properties.FdoProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FdoRecordComponentTest {

  @Mock
  private FdoProperties fdoProperties;

  @Test
  void testGetRequest() throws Exception {
    // Given
    given(fdoProperties.getAgent()).willReturn("https://ror.org/0566bfb96");
    var fdoComponent = new FdoRecordComponent(MAPPER, fdoProperties);

    // When
    var result = fdoComponent.getPostRequest();

    // Then
    assertThat(result).isEqualTo(givenPostHandleRequest());
  }

}
