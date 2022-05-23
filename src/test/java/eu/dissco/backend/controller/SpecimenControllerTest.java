package eu.dissco.backend.controller;

import static eu.dissco.backend.util.TestUtils.CURATED_OBJECT_ID;
import static eu.dissco.backend.util.TestUtils.ID;
import static eu.dissco.backend.util.TestUtils.IMAGE_URL;
import static eu.dissco.backend.util.TestUtils.INSTITUTION;
import static eu.dissco.backend.util.TestUtils.INSTITUTION_CODE;
import static eu.dissco.backend.util.TestUtils.MATERIAL_TYPE;
import static eu.dissco.backend.util.TestUtils.MIDS_LEVEL;
import static eu.dissco.backend.util.TestUtils.NAME;
import static eu.dissco.backend.util.TestUtils.PHYSICAL_SPECIMEN_ID;
import static eu.dissco.backend.util.TestUtils.TYPE;
import static eu.dissco.backend.util.TestUtils.givenDigitalSpecimen;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.service.SpecimenService;
import java.util.List;
import net.cnri.cordra.api.CordraException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest
@ContextConfiguration(classes = {SpecimenController.class,
    RestResponseEntityExceptionHandler.class})
class SpecimenControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private SpecimenService service;

  @Test
  void testGetSpecimen() throws Exception {
    // Given
    given(service.getSpecimen(anyInt(), anyInt())).willReturn(
        List.of(givenDigitalSpecimen()));

    // When
    var result = this.mockMvc.perform(get("/api/v1/specimen/"));

    // Then
    validateResult(result, "$.[0]");
  }

  @Test
  void testGetSpecimenById() throws Exception {
    // Given
    given(service.getSpecimenById(anyString())).willReturn(givenDigitalSpecimen());

    // When
    var result = this.mockMvc.perform(get("/api/v1/specimen/" + ID));

    // Then
    validateResult(result, "$");
  }

  @Test
  void testSearchQuery() throws Exception {
    // Given
    given(service.search(anyString(), anyInt(), anyInt())).willReturn(
        List.of(givenDigitalSpecimen()));

    // When
    var result = this.mockMvc.perform(get("/api/v1/specimen/search?query=" + ID));

    // Then
    validateResult(result, "$.[0]");
  }

  @Test
  void testCordraException() throws Exception {
    // Given
    given(service.search(anyString(), anyInt(), anyInt())).willThrow(CordraException.class);

    // When
    var result = this.mockMvc.perform(get("/api/v1/specimen/search?query=" + ID));

    // Then
    result.andExpect(status().isInternalServerError());
  }

  @Test
  void testJsonException() throws Exception {
    // Given
    given(service.search(anyString(), anyInt(), anyInt())).willThrow(JsonProcessingException.class);

    // When
    var result = this.mockMvc.perform(get("/api/v1/specimen/search?query=" + ID));

    // Then
    result.andExpect(status().isUnprocessableEntity());
  }

  private void validateResult(ResultActions result, String jsonpathPrefix) throws Exception {
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath(jsonpathPrefix + ".@id").value(ID))
        .andExpect(jsonPath(jsonpathPrefix + ".@type").value(TYPE))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:authoritative.ods:midsLevel").value(MIDS_LEVEL))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:authoritative.ods:curatedObjectID").value(
            CURATED_OBJECT_ID))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:authoritative.ods:physicalSpecimenId").value(
            PHYSICAL_SPECIMEN_ID))
        .andExpect(
            jsonPath(jsonpathPrefix + ".ods:authoritative.ods:institution").value(INSTITUTION))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:authoritative.ods:institutionCode").value(
            INSTITUTION_CODE))
        .andExpect(
            jsonPath(jsonpathPrefix + ".ods:authoritative.ods:materialType").value(MATERIAL_TYPE))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:authoritative.ods:name").value(NAME))
        .andExpect(jsonPath(jsonpathPrefix + ".ods:images.[0].ods:imageURI").value(IMAGE_URL));
  }


}
