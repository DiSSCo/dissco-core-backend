package eu.dissco.backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObjectWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import java.util.ArrayList;
import java.util.List;

import static eu.dissco.backend.TestUtils.*;

public class DigitalMediaObjectUtils {

  public static final String DIGITAL_MEDIA_URI = "/api/v1/digitalmedia";
  public static final String DIGITAL_MEDIA_PATH = SANDBOX_URI + DIGITAL_MEDIA_URI;

  // Media Object
  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String id) {
    return new DigitalMediaObjectWrapper(id, 1, CREATED, "2DImageObject", ID_ALT,
        "https://dissco.com", "image/jpeg", "20.5000.1025/GW0-TYL-YRU",
        givenDigitalMediaObjectData(), givenDigitalMediaObjectOriginalData());
  }

  public static DigitalMediaObjectWrapper givenDigitalMediaObject(String mediaId, String specimenId) {
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
    return new DigitalMediaObjectWrapper(mediaId, version, CREATED, "2DImageObject", specimenId,
        "https://dissco.com", "image/jpeg", sourceSystemId,
        givenDigitalMediaObjectData(), givenDigitalMediaObjectOriginalData());
  }

  // Media Object Construction Helpers
  private static JsonNode givenDigitalMediaObjectData() {
    ObjectNode data = MAPPER.createObjectNode();
    data.put("dcterms:title", "19942272");
    data.put("dcterms:publisher", "Royal Botanic Garden Edinburgh");
    return data;
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
    return new JsonApiData(mediaObject.id(), mediaObject.type(), mediaObject, MAPPER);
  }

  // JsonApiWrapper

  public static JsonApiWrapper givenDigitalMediaJsonResponse(String path, String mediaId) {
    JsonApiLinks linksNode = new JsonApiLinks(path);
    var mediaObject = givenDigitalMediaObject(mediaId);
    JsonApiData dataNode = new JsonApiData(mediaId, mediaObject.type(), mediaObject, MAPPER);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  // JsonApiListResponseWrapper

  public static JsonApiListResponseWrapper givenDigitalMediaJsonResponse(String path,
      int pageNumber,
      int pageSize, List<String> mediaIds) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, true, path);
    List<JsonApiData> dataNode = new ArrayList<>();
    for (String id : mediaIds) {
      var mediaObject = givenDigitalMediaObject(id);
      dataNode.add(new JsonApiData(id, "2DImageObject", mediaObject, MAPPER));
    }
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

}
