package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.MAPPER;

import com.fasterxml.jackson.databind.JsonNode;

public class HandleUtils {
  private HandleUtils(){}

  public static JsonNode givenHandleRequestFullTypeStatus() throws Exception {
    return MAPPER.readTree("""
        {
          "data": {
            "type": "digitalSpecimen",
            "attributes": {
              "fdoProfile": "https://hdl.handle.net/21.T11148/d8de0819e144e4096645",
              "digitalObjectType": "https://hdl.handle.net/21.T11148/894b1e6cad57e921764e",
              "issuedForAgent": "https://ror.org/0566bfb96",
              "primarySpecimenObjectId": "https://geocollections.info/specimen/23602",
              "normalisedPrimarySpecimenObjectId":"https://geocollections.info/specimen/23602",
              "primarySpecimenObjectIdType": "Global",
              "specimenHost": "https://ror.org/0443cwa12",
              "specimenHostName": "National Museum of Natural History",
              "topicDiscipline": "Botany",
              "referentName": "Biota",
              "livingOrPreserved": "Preserved",
              "markedAsType":true
            }
          }
        }""");
  }

  public static JsonNode givenHandleRequestMin() throws Exception {
    return MAPPER.readTree("""
        {
          "data": {
            "type": "digitalSpecimen",
            "attributes": {
              "fdoProfile": "https://hdl.handle.net/21.T11148/d8de0819e144e4096645",
              "digitalObjectType": "https://hdl.handle.net/21.T11148/894b1e6cad57e921764e",
              "issuedForAgent": "https://ror.org/0566bfb96",
              "primarySpecimenObjectId": "https://geocollections.info/specimen/23602",
              "normalisedPrimarySpecimenObjectId": "https://geocollections.info/specimen/23602",
              "primarySpecimenObjectIdType":"Local",
              "specimenHost": "https://ror.org/0443cwa12"
            }
          }
        }
        """);
  }

  public static JsonNode givenHandleRequest() throws Exception {
    return MAPPER.readTree("""
        {
          "data": [
            {
              "id": "20.5000.1025/V1Z-176-LL4",
              "type": "digitalSpecimen",
              "attributes": {
                "fdoProfile": "https://hdl.handle.net/21.T11148/d8de0819e144e4096645",
                "digitalObjectType": "https://hdl.handle.net/21.T11148/894b1e6cad57e921764e",
                "issuedForAgent": "https://ror.org/0566bfb96",
                "primarySpecimenObjectId": "https://geocollections.info/specimen/23602",
                "specimenHost": "https://ror.org/0443cwa12",
                "specimenHostName": "National Museum of Natural History",
                "primarySpecimenObjectIdType": "Global",
                "referentName": "Biota",
                "topicDiscipline": "Earth Systems",
                "livingOrPreserved": "Living",
                "markedAsType": true
              }
            }
          ]
        }
        """);
  }



}
