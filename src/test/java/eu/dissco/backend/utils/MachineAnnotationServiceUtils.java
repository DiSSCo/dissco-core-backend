package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.MAS_ID;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.utils.MasJobRecordUtils.TTL_DEFAULT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.MachineAnnotationService;
import eu.dissco.backend.schema.MachineAnnotationService.OdsStatus;
import eu.dissco.backend.schema.OdsTargetDigitalObjectFilter;
import eu.dissco.backend.schema.SchemaContactPoint;
import eu.dissco.backend.schema.TombstoneMetadata;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MachineAnnotationServiceUtils {

  public static JsonApiListResponseWrapper givenMasResponse(String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(givenMas().getId(), "MachineAnnotationService", givenMas(),
            MAPPER));
    return new JsonApiListResponseWrapper(masRecords, links, new JsonApiMeta(masRecords.size()));
  }

  public static JsonApiListResponseWrapper givenMasResponse(
      MachineAnnotationService masRecord,
      String path) {
    var links = new JsonApiLinksFull(path);
    var masRecords = List.of(
        new JsonApiData(masRecord.getId(), "ods:MachineAnnotationService", masRecord, MAPPER));
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
        MAS_ID,
        batching,
        ttl);
  }

  public static MachineAnnotationService givenMas() {
    return givenMas(ID, null);
  }

  public static MachineAnnotationService givenMas(String id, Instant deleted) {
    return givenMas(
        id,
        deleted,
        new OdsTargetDigitalObjectFilter(),
        false,
        TTL_DEFAULT
    );
  }

  public static MachineAnnotationService givenMas(OdsTargetDigitalObjectFilter filters,
      boolean batching) {
    return givenMas(
        MAS_ID,
        null,
        filters,
        batching,
        TTL_DEFAULT
    );
  }

  public static MachineAnnotationService givenMas(OdsTargetDigitalObjectFilter filters) {
    return givenMas(MAS_ID, null, filters, false, 3600);
  }

  public static MachineAnnotationService givenMas(String id, Instant deleted,
      OdsTargetDigitalObjectFilter filters, boolean batching, int ttl) {
    var mas = new MachineAnnotationService()
        .withId(id)
        .withOdsID(id)
        .withType("ods:MachineAnnotationService")
        .withOdsType("https://doi.org/21.T11148/894b1e6cad57e921764e")
        .withOdsStatus(OdsStatus.ODS_ACTIVE)
        .withSchemaVersion(1)
        .withSchemaName("A Machine Annotation Service")
        .withSchemaDescription("A fancy mas making all dreams come true")
        .withSchemaDateCreated(Date.from(CREATED))
        .withSchemaDateModified(Date.from(CREATED))
        .withSchemaCreator(new Agent().withType(Type.SCHEMA_PERSON).withId(ORCID))
        .withOdsContainerImage("public.ecr.aws/dissco/fancy-mas")
        .withOdsContainerTag("sha-54289")
        .withOdsTargetDigitalObjectFilter(filters)
        .withSchemaCreativeWorkStatus("Definitely production ready")
        .withSchemaCodeRepository("https://github.com/DiSSCo/fancy-mas")
        .withSchemaProgrammingLanguage("Java")
        .withOdsServiceAvailability("public")
        .withSchemaMaintainer(new Agent().withType(Type.SCHEMA_PERSON).withId(ORCID))
        .withSchemaLicense("https://www.apache.org/licenses/LICENSE-2.0")
        .withOdsDependency(List.of())
        .withSchemaContactPoint(new SchemaContactPoint().withSchemaEmail("dontmail@dissco.eu"))
        .withOdsSlaDocumentation("https://www.know.dissco.tech/no_sla")
        .withOdsTopicName("fancy-topic-name")
        .withOdsBatchingPermitted(batching)
        .withOdsTimeToLive(ttl);
    if (deleted != null) {
      mas.setOdsStatus(OdsStatus.ODS_TOMBSTONE);
      mas.setOdsTombstoneMetadata(
          new TombstoneMetadata().withOdsTombstoneDate(Date.from(deleted)));
    }
    return mas;
  }

  public static JsonNode givenFlattenedDigitalMedia() throws JsonProcessingException {
    return MAPPER.readValue(
        """
                {
                  "@id": "https://doi.org/TEST/SDF-6Y6-DV7",
                  "@type": "ods:DigitalMedia",
                  "ods:ID": "https://doi.org/TEST/SDF-6Y6-DV7",
                  "ods:version": 1,
                  "dcterms:created": "2023-10-16T11:47:18.773831Z",
                  "ods:type": "https://doi.org/21.T11148/bbad8c4e101e8af01115",
                  "ac:accessUri": "https://herbarium.bgbm.org/data/iiif/BW00746010/manifest.json",
                  "dwc:organisationID": "https://ror.org/0349vqz63",
                  "dwc:organisationName": "Royal Botanic Garden Edinburgh Herbarium",
                  "dcterms:format": "application/json",
                  "dcterms:license": "https://creativecommons.org/licenses/by-sa/3.0/",
                  "dcterms:source": "https://iiif.bgbm.org/?manifest=https://herbarium.bgbm.org/object/BW00746010/manifest.json",
                  "digitalSpecimen":                 {
                    "@id": "20.5000.1025/ABC-123-XYZ",
                    "@type": "ods:DigitalSpecimen",
                    "ods:ID": "20.5000.1025/ABC-123-XYZ",
                    "ods:version": 1,
                    "dcterms:modified": "03/12/2012",
                    "dcterms:created": "2022-11-01T09:59:24.000Z",
                    "ods:type": "https://doi.org/21.T11148/894b1e6cad57e921764e",
                    "ods:midsLevel": 0,
                    "ods:physicalSpecimenID": "global_id_123123",
                    "ods:physicalSpecimenIDType": "Resolvable",
                    "ods:isMarkedAsType": true,
                    "ods:isKnownToContainMedia": true,
                    "ods:specimenName": "Abyssothyris Thomson, 1927",
                    "ods:sourceSystemID": "https://hdl.handle.net/20.5000.1025/3XA-8PT-SAY",
                    "ods:language": [],
                    "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
                    "dwc:preparations": "",
                    "ods:organisationID": "https://ror.org/0349vqz63",
                    "ods:organisationName": "Royal Botanic Garden Edinburgh Herbarium",
                    "dwc:datasetName": "Royal Botanic Garden Edinburgh Herbarium",
                    "ods:hasMaterialEntity": [],
                    "ods:hasIdentification": [],
                    "ods:hasAssertion": [],
                    "ods:hasEvent": [
                      {
                        "ods:hasAssertion": [],
                        "ods:Location": {
                          "dwc:country": "Scotland"
                        }
                      }
                    ],
                    "ods:hasEntityRelationship": [],
                    "ods:hasCitation": [],
                    "ods:hasIdentifier": [],
                    "ods:hasChronometricAge": [],
                    "ods:hasAgent": []
                  }
                }
            """, JsonNode.class
    );
  }

  public static JsonNode givenFlattenedDigitalSpecimen() throws JsonProcessingException {
    return MAPPER.readValue(
        """
                {
                  "@id": "20.5000.1025/ABC-123-XYZ",
                  "@type": "ods:DigitalSpecimen",
                  "ods:ID": "20.5000.1025/ABC-123-XYZ",
                  "ods:version": 1,
                  "dcterms:modified": "03/12/2012",
                  "dcterms:created": "2022-11-01T09:59:24.000Z",
                  "ods:type": "https://doi.org/21.T11148/894b1e6cad57e921764e",
                  "ods:midsLevel": 0,
                  "ods:physicalSpecimenID": "global_id_123123",
                  "ods:physicalSpecimenIDType": "Resolvable",
                  "ods:isMarkedAsType": true,
                  "ods:topicDiscipline": "Palaeontology",
                  "ods:isKnownToContainMedia": true,
                  "ods:specimenName": "Abyssothyris Thomson, 1927",
                  "ods:sourceSystemID": "https://hdl.handle.net/20.5000.1025/3XA-8PT-SAY",
                  "ods:language": [],
                  "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
                  "dwc:preparations": "",
                  "ods:organisationID": "https://ror.org/0349vqz63",
                  "ods:organisationName": "Royal Botanic Garden Edinburgh Herbarium",
                  "dwc:datasetName": "Royal Botanic Garden Edinburgh Herbarium",
                  "ods:hasMaterialEntity": [],
                  "ods:hasIdentification": [],
                  "ods:hasAssertion": [],
                  "ods:hasEvent": [
                    {
                      "ods:hasAssertion": [],
                      "ods:Location": {
                        "dwc:country": "Scotland"
                      }
                    }
                  ],
                  "ods:hasEntityRelationship": [],
                  "ods:hasCitation": [],
                  "ods:hasIdentifier": [],
                  "ods:hasChronometricAge": [],
                  "ods:hasAgent": []
                }
            """, JsonNode.class
    );
  }


}
