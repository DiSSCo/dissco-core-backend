package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObjectWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.schema.DigitalEntity;
import eu.dissco.backend.schema.EntityRelationships;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DigitalMediaObjectUtils {

  public static final String DIGITAL_MEDIA_URI = "/api/v1/digitalmedia";
  public static final String DIGITAL_MEDIA_PATH = SANDBOX_URI + DIGITAL_MEDIA_URI;
  public static final String DIGITAL_MEDIA_FDO_TYPE = "https://doi.org/21.T11148/bbad8c4e101e8af01115";

  // Media Object
  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String id) {
    return new DigitalMediaObjectWrapper(
        givenDigitalMediaObjectData(id, 1, CREATED, DIGITAL_MEDIA_FDO_TYPE, DOI + ID_ALT,
            "https://dissco.com", "image/jpeg"),
        givenDigitalMediaObjectOriginalData());
  }

  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String mediaId,
      String specimenId) {
    return givenDigitalMediaObject(mediaId, specimenId, SOURCE_SYSTEM_ID_1, 1);
  }

  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String mediaId, String specimenId,
      int version) {
    return givenDigitalMediaObject(mediaId, specimenId, SOURCE_SYSTEM_ID_1, version);
  }

  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String mediaId, String specimenId,
      String sourceSystemId) {
    return givenDigitalMediaObject(mediaId, specimenId, sourceSystemId, 1);
  }

  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String mediaId, String specimenId,
      String sourceSystemId, int version) {
    return new DigitalMediaObjectWrapper(
        givenDigitalMediaObjectData(mediaId, version, CREATED, DIGITAL_MEDIA_FDO_TYPE, specimenId,
            "https://dissco.com", "image/jpeg"),
        givenDigitalMediaObjectOriginalData());
  }

  private static JsonNode givenDigitalMediaObjectOriginalData() {
    ObjectNode originalData = MAPPER.createObjectNode();
    originalData.put("dcterms:title", "19942272");
    originalData.put("dcterms:type", "StillImage");
    return originalData;
  }

  // JsonApiData
  public static JsonApiData givenDigitalMediaJsonApiData(String id) {
    var mediaObject = givenDigitalMediaObject(id);
    return new JsonApiData(mediaObject.digitalEntity().getOdsId(), mediaObject.digitalEntity()
        .getOdsType(), mediaObject, MAPPER);
  }

  public static JsonApiWrapper givenDigitalMediaJsonResponse(String path, String mediaId) {
    JsonApiLinks linksNode = new JsonApiLinks(path);
    var mediaObject = givenDigitalMediaObject(mediaId);
    JsonApiData dataNode = new JsonApiData(mediaId, mediaObject.digitalEntity().getOdsType(),
        mediaObject, MAPPER);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  // JsonApiWrapper

  public static JsonApiListResponseWrapper givenDigitalMediaJsonResponse(String path,
      int pageNumber,
      int pageSize, List<String> mediaIds) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, true, path);
    List<JsonApiData> dataNode = new ArrayList<>();
    for (String id : mediaIds) {
      var mediaObject = givenDigitalMediaObject(id);
      dataNode.add(new JsonApiData(id, "StillImage", mediaObject, MAPPER));
    }
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

  private static DigitalEntity givenDigitalMediaObjectData(String mediaId, int version,
      Instant created, String type, String specimenId, String url, String contentType) {
    return new DigitalEntity()
        .withOdsId(mediaId)
        .withOdsVersion(version)
        .withOdsCreated(created.toString())
        .withOdsType(type)
        .withAcAccessUri(url)
        .withDctermsFormat(contentType)
        .withEntityRelationships(List.of(
            new EntityRelationships().withEntityRelationshipType("hasDigitalSpecimen")
                .withObjectEntityIri(specimenId)));
  }

}
