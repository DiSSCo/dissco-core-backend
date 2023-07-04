package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.MachineAnnotationService;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MachineAnnotationServiceUtils {

  public static JsonApiListResponseWrapper givenMasResponse(String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(givenMasRecord().id(), "MachineAnnotationService", givenMasRecord(),
            MAPPER));
    return new JsonApiListResponseWrapper(masRecords, links, new JsonApiMeta(masRecords.size()));
  }

  public static JsonApiListResponseWrapper givenMasResponse(MachineAnnotationServiceRecord masRecord,
                                                            String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(masRecord.id(), "MachineAnnotationService", masRecord, MAPPER));
    return new JsonApiListResponseWrapper(masRecords, links, new JsonApiMeta(masRecords.size()));
  }

  public static JsonApiRequestWrapper givenMasRequest() {
    return givenMasRequest("MasRequest");
  }

  public static JsonApiRequestWrapper givenMasRequest(String type) {
    var mass = Map.of("mass", List.of(ID));
    var apiRequest = new JsonApiRequest(type, MAPPER.valueToTree(mass));
    return new JsonApiRequestWrapper(apiRequest);
  }

  public static MachineAnnotationServiceRecord givenMasRecord() {
    return givenMasRecord(ID, null);
  }

  public static MachineAnnotationServiceRecord givenMasRecord(String id, Instant deleted) {
    return new MachineAnnotationServiceRecord(
        id,
        1,
        CREATED,
        USER_ID_TOKEN,
        givenMas(),
        deleted
    );
  }

  public static MachineAnnotationServiceRecord givenMasRecord(JsonNode filters) {
    return new MachineAnnotationServiceRecord(
        ID,
        1,
        CREATED,
        USER_ID_TOKEN,
        givenMas(filters),
        null
    );
  }

  public static MachineAnnotationService givenMas() {
    return givenMas(MAPPER.createObjectNode());
  }

  public static MachineAnnotationService givenMas(JsonNode filters) {
    return new MachineAnnotationService(
        "A Machine Annotation Service",
        "public.ecr.aws/dissco/fancy-mas",
        "sha-54289",
        filters,
        "A fancy mas making all dreams come true",
        "Definitely production ready",
        "https://github.com/DiSSCo/fancy-mas",
        "public",
        "No one we know",
        "https://www.apache.org/licenses/LICENSE-2.0",
        List.of(),
        "dontmail@dissco.eu",
        "https://www.know.dissco.tech/no_sla",
        "fancy-topic-name",
        5
    );
  }

  public static JsonNode givenFlattenedDigitalMedia() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "type": "2DImageObject",
              "dcterms:title": "19942272",
              "dcterms:publisher": "Royal Botanic Garden Edinburgh",
              "specimen.type": "BotanySpecimen",
              "specimen.dwca:id": "http://data.rbge.org.uk/herb/E00586417",
              "specimen.ods:modified": "03/12/2012",
              "specimen.ods:datasetId": "Royal Botanic Garden Edinburgh Herbarium",
              "specimen.ods:objectType": "",
              "specimen.dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
              "specimen.ods:specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
              "specimen.ods:organisationId": "https://ror.org/0349vqz63",
              "specimen.ods:sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
              "specimen.ods:physicalSpecimenIdType": "cetaf",
              "specimen.ods:physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670",
              "specimen.dwc:typeStatus": "holotype",
              "specimen.dwc:country": "Scotland",
              "specimen.ods:hasMedia": "true"
            }
            """, JsonNode.class
    );
  }

  public static JsonNode givenFlattenedDigitalSpecimen() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "type": "BotanySpecimen",
              "dwca:id": "http://data.rbge.org.uk/herb/E00586417",
              "ods:modified": "03/12/2012",
              "ods:datasetId": "Royal Botanic Garden Edinburgh Herbarium",
              "ods:objectType": "",
              "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
              "ods:specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
              "ods:organisationId": "https://ror.org/0349vqz63",
              "ods:sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
              "ods:physicalSpecimenIdType": "cetaf",
              "ods:physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670",
              "dwc:typeStatus": "holotype",
              "dwc:country": "Scotland",
              "ods:hasMedia": "true"
            }
            """, JsonNode.class
    );
  }


}
