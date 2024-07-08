package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;

import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.schema.DigitalMedia;
import eu.dissco.backend.schema.DigitalMedia.DctermsType;
import eu.dissco.backend.schema.DigitalMedia.OdsStatus;
import eu.dissco.backend.schema.EntityRelationship;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DigitalMediaObjectUtils {

  public static final String DIGITAL_MEDIA_URI = "/api/v1/digitalmedia";
  public static final String DIGITAL_MEDIA_PATH = SANDBOX_URI + DIGITAL_MEDIA_URI;
  public static final String DIGITAL_MEDIA_FDO_TYPE = "https://doi.org/21.T11148/bbad8c4e101e8af01115";

  // Media Object
  public static DigitalMedia givenDigitalMediaObject(String id) {
    return givenDigitalMediaObjectData(id, 1, CREATED, DIGITAL_MEDIA_FDO_TYPE, DOI + ID_ALT,
            "https://dissco.com", "image/jpeg");
  }

  public static DigitalMedia givenDigitalMediaObject(String mediaId, String specimenId) {
    return givenDigitalMediaObject(mediaId, specimenId, 1);
  }

  public static DigitalMedia givenDigitalMediaObject(String mediaId, String specimenId, int version) {
    return givenDigitalMediaObjectData(mediaId, version, CREATED, DIGITAL_MEDIA_FDO_TYPE, specimenId,
        "https://dissco.com", "image/jpeg");
  }

  // JsonApiData
  public static JsonApiData givenDigitalMediaJsonApiData(String id) {
    var mediaObject = givenDigitalMediaObject(id);
    return new JsonApiData(mediaObject.getOdsID(), mediaObject.getOdsType(), mediaObject, MAPPER);
  }

  public static JsonApiWrapper givenDigitalMediaJsonResponse(String path, String mediaId) {
    JsonApiLinks linksNode = new JsonApiLinks(path);
    var mediaObject = givenDigitalMediaObject(mediaId);
    JsonApiData dataNode = new JsonApiData(mediaId, mediaObject.getOdsType(), mediaObject, MAPPER);
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

  private static DigitalMedia givenDigitalMediaObjectData(String mediaId, int version,
      Instant created, String type, String specimenId, String url, String contentType) {
    return new DigitalMedia()
        .withId(mediaId)
        .withType("ods:DigitalMedia")
        .withOdsID(mediaId)
        .withOdsVersion(version)
        .withOdsStatus(OdsStatus.ODS_ACTIVE)
        .withOdsCreated(Date.from(created))
        .withOdsType(type)
        .withAcAccessURI(url)
        .withDctermsType(DctermsType.STILL_IMAGE)
        .withDctermsFormat(contentType)
        .withOdsSourceSystemID(SOURCE_SYSTEM_ID_1)
        .withOdsHasEntityRelationship(List.of(
            new EntityRelationship().withType("ods:EntityRelationship")
                .withDwcRelationshipOfResource("hasDigitalSpecimen")
                .withDwcRelatedResourceID(specimenId)));
  }

}
