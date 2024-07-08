package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.MachineAnnotationService;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRequest;
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

  public static JsonApiListResponseWrapper givenMasResponse(
      MachineAnnotationServiceRecord masRecord,
      String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(masRecord.id(), "MachineAnnotationService", masRecord, MAPPER));
    return new JsonApiListResponseWrapper(masRecords, links, new JsonApiMeta(masRecords.size()));
  }

  public static JsonApiListResponseWrapper givenScheduledMasResponse(MasJobRecord masJobRecord,
      String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(masJobRecord.jobId(), "MachineAnnotationServiceJobRecord", masJobRecord,
            MAPPER));
    return new JsonApiListResponseWrapper(masRecords, links, new JsonApiMeta(masRecords.size()));
  }

  public static JsonApiRequestWrapper givenMasRequest() {
    return givenMasRequest("MasRequest");
  }

  public static JsonApiRequestWrapper givenMasRequest(String type) {
    var mass = Map.of("mass", List.of(givenMasJobRequest()));
    var apiRequest = new JsonApiRequest(type, MAPPER.valueToTree(mass));
    return new JsonApiRequestWrapper(apiRequest);
  }

  public static MasJobRequest givenMasJobRequest() {
    return givenMasJobRequest(false, null);
  }

  public static MasJobRequest givenMasJobRequest(boolean batching, Long ttl) {
    return new MasJobRequest(
        ID,
        batching,
        ttl);
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
    return givenMasRecord(filters, false);
  }

  public static MachineAnnotationServiceRecord givenMasRecord(JsonNode filters, boolean batching) {
    return new MachineAnnotationServiceRecord(
        ID,
        1,
        CREATED,
        USER_ID_TOKEN,
        givenMas(filters, batching),
        null
    );
  }

  public static MachineAnnotationService givenMas() {
    return givenMas(MAPPER.createObjectNode());
  }

  public static MachineAnnotationService givenMas(JsonNode filters) {
    return givenMas(filters, false);
  }

  public static MachineAnnotationService givenMas(JsonNode filters, boolean batching) {
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
        5,
        batching
    );
  }

  public static JsonNode givenFlattenedDigitalMedia() throws JsonProcessingException {
    return MAPPER.readValue(
        """
                {
                  "ods:id": "https://doi.org/TEST/SDF-6Y6-DV7",
                  "ods:version": 1,
                  "dcterms:created": "2023-10-16T11:47:18.773831Z",
                  "ods:type": "https://doi.org/21.T11148/bbad8c4e101e8af01115",
                  "ac:accessUri": "https://herbarium.bgbm.org/data/iiif/BW00746010/manifest.json",
                  "dwc:institutionId": "https://ror.org/00bv4cx53",
                  "dwc:institutionName": "Botanic Garden and Botanical Museum Berlin",
                  "dcterms:format": "application/json",
                  "dcterms:license": "https://creativecommons.org/licenses/by-sa/3.0/",
                  "dcterms:source": "https://iiif.bgbm.org/?manifest=https://herbarium.bgbm.org/object/BW00746010/manifest.json",
                  "digitalSpecimen": {
                    "ods:type": "https://doi.org/21.T11148/894b1e6cad57e921764e",
                    "occurrences": [
                      {
                        "dwc:habitat": "Venezuela.",
                        "assertions": [],
                        "location": {
                          "dwc:continent": "Middle and South America",
                          "dwc:country": "Venezuela"
                        }
                      },
                      {
                        "dwc:habitat": "Argentina.",
                        "assertions": [],
                        "location": {
                          "dwc:continent": "South America",
                          "dwc:country": "Argentina"
                        }
                      }
                    ],
                    "ods:hasMedia": "true"
                  }
                }
            """, JsonNode.class
    );
  }

  public static JsonNode givenFlattenedDigitalSpecimen() throws JsonProcessingException {
    return MAPPER.readValue(
        """
                {
                  "ods:id": "https://doi.org/TEST/JDS-HJL-SJD",
                  "ods:version": 1,
                  "dcterms:created": "2023-10-16T12:46:28.460956Z",
                  "ods:type": "https://doi.org/21.T11148/894b1e6cad57e921764e",
                  "ods:midsLevel": 0,
                  "ods:topicDiscipline": "Palaeontology",
                  "ods:hasMedia": false,
                  "ods:specimenName": "Graptolithina",
                  "ods:sourceSystem": "https://hdl.handle.net/TEST/6JE-97W-RDY",
                  "ods:livingOrPreserved": "Preserved",
                  "dcterms:license": "http://creativecommons.org/licenses/by-nc/4.0/",
                  "dcterms:modified": "1220960707000",
                  "dwc:basisOfRecord": "FossilSpecimen",
                  "dwc:preparations": "",
                  "dwc:institutionId": "https://ror.org/0443cwa12",
                  "dwc:institutionName": "Tallinn University of Technology",
                  "dwc:datasetName": "TalTech geological collections",
                  "materialEntity": [],
                  "dwc:identification": [
                    {
                      "dwc:identificationVerificationStatus": true,
                      "citations": [],
                      "taxonIdentifications": [
                        {
                          "dwc:scientificName": "Graptolithina"
                        }
                      ]
                    }
                  ],
                  "assertions": [],
                  "occurrences": [
                    {
                      "assertions": [],
                      "location": {
                        "dwc:country": "Latvia",
                        "dwc:locality": "Ventspils D-3 borehole",
                        "dwc:minimumElevationInMeters": 2,
                        "dwc:maximumElevationInMeters": 2,
                        "dwc:minimumDepthInMeters": 607.5,
                        "georeference": {
                          "dwc:decimalLatitude": 57.411518,
                          "dwc:decimalLongitude": 21.569814,
                          "dwc:geodeticDatum": "WGS84"
                        },
                        "geologicalContext": {
                          "dwc:earliestEpochOrLowestSeries": "Ludlow",
                          "dwc:latestEpochOrHighestSeries": "Ludlow"
                        }
                      }
                    }
                  ]
                }
            """, JsonNode.class
    );
  }


}
