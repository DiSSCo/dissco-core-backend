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
class FdoComponentTest {

  @Mock
  private FdoProperties fdoProperties;

  @Test
  void testGetRequest() throws Exception {
    // Given
    given(fdoProperties.getAgent()).willReturn("https://ror.org/0566bfb96");
    given(fdoProperties.getProfile()).willReturn(
        "https://hdl.handle.net/21.T11148/64396cf36b976ad08267");
    given(fdoProperties.getDigitalObjectType()).willReturn("https://hdl.handle.net/21.T11148/64396cf36b976ad08267");
    var fdoComponent = new FdoComponent(MAPPER, fdoProperties);

    // When
    var result = fdoComponent.getPostRequest();

    // Then
    assertThat(result).isEqualTo(givenPostHandleRequest());

  }

}
