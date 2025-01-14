package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.PHYSICAL_ID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.service.ElvisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ElvisControllerTest {

  @Mock
  private ElvisService elvisService;
  private ElvisController elvisController;

  @BeforeEach
  void setup(){
    elvisController = new ElvisController(elvisService);
  }

  @Test
  void testGetSpecimenByInventoryNumber() throws Exception {
    // When
    var result = elvisController.getSpecimenByInventoryNumber(PHYSICAL_ID, 1,1);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByDoi() {
    // When
    var result = elvisController.getSpecimenByDoi(PREFIX, SUFFIX);

    // Then
    then(elvisService).should().searchByDoi(ID);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testSuggestInventoryNumber() throws Exception {
    // When
    var result = elvisController.suggestInventoryNumber(PHYSICAL_ID, 1, 1);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


}
