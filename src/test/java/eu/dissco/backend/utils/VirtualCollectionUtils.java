package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.givenCreator;

import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.schema.OdsHasPredicate;
import eu.dissco.backend.schema.OdsHasPredicate.OdsPredicateType;
import eu.dissco.backend.schema.TargetDigitalObjectFilter;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollection.LtcBasisOfScheme;
import eu.dissco.backend.schema.VirtualCollection.OdsStatus;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class VirtualCollectionUtils {

  public static final String VIRTUAL_COLLECTION_URI = "/api/v1/virtual-collection/";
  public static final String VIRTUAL_COLLECTION_PATH = SANDBOX_URI + VIRTUAL_COLLECTION_URI;
  public static final String VIRTUAL_COLLECTION_NAME = "Butterflies of the World";
  public static final String VIRTUAL_COLLECTION_DESCRIPTION =
      "A collection of butterflies from around the world";

  private VirtualCollectionUtils() {
  }

  public static List<VirtualCollection> givenVirtualCollectionResponseList(
      String virtualCollectionID, int n) {
    return Collections.nCopies(n, givenVirtualCollectionResponse(virtualCollectionID));
  }

  public static VirtualCollection givenVirtualCollectionResponse(String virtualCollectionID) {
    return givenVirtualCollectionResponse(virtualCollectionID, ORCID);
  }

  public static VirtualCollection givenVirtualCollectionResponse(String virtualCollectionID,
      String userId) {
    return new VirtualCollection()
        .withId(virtualCollectionID)
        .withDctermsIdentifier(virtualCollectionID)
        .withType("ods:VirtualCollection")
        .withSchemaVersion(1)
        .withOdsFdoType("https://hdl.handle.net/21.T11148/2ac65a933b7a0361b651")
        .withOdsStatus(OdsStatus.ACTIVE)
        .withLtcCollectionName(VIRTUAL_COLLECTION_NAME)
        .withLtcDescription(VIRTUAL_COLLECTION_DESCRIPTION)
        .withLtcBasisOfScheme(LtcBasisOfScheme.REFERENCE_COLLECTION)
        .withSchemaDateCreated(Date.from(CREATED))
        .withSchemaDateModified(Date.from(CREATED))
        .withSchemaCreator(givenCreator(userId))
        .withOdsHasTargetDigitalObjectFilter(givenTargetFilter());
  }

  public static TargetDigitalObjectFilter givenTargetFilter() {
    return new TargetDigitalObjectFilter()
        .withOdsHasPredicates(List.of(new OdsHasPredicate()
            .withOdsPredicateType(OdsPredicateType.EQUALS)
            .withOdsPredicateKey("$['ods:topicDiscipline']")
            .withOdsPredicateValue("zoology")));
  }

  public static VirtualCollectionRequest givenVirtualCollectionRequest() {
    return givenVirtualCollectionRequest(VIRTUAL_COLLECTION_NAME,
        VirtualCollectionRequest.LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter());
  }

  public static VirtualCollectionRequest givenVirtualCollectionRequest(String name,
      VirtualCollectionRequest.LtcBasisOfScheme ltcBasisOfScheme,
      TargetDigitalObjectFilter targetFilter) {
    return new VirtualCollectionRequest()
        .withLtcCollectionName(name)
        .withLtcDescription(VIRTUAL_COLLECTION_DESCRIPTION)
        .withLtcBasisOfScheme(ltcBasisOfScheme)
        .withOdsHasTargetDigitalObjectFilter(targetFilter);
  }

  public static JsonApiWrapper givenVirtualCollectionResponseWrapper(String path) {
    return givenVirtualCollectionResponseWrapper(path, ORCID);
  }

  public static JsonApiWrapper givenVirtualCollectionResponseWrapper(
      String path, String orcid) {
    var virtualCollection = givenVirtualCollectionResponse(HANDLE + ID, orcid);
    var dataNode = new JsonApiData(HANDLE + ID, "ods:VirtualCollection",
        MAPPER.valueToTree(virtualCollection));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public static JsonApiListResponseWrapper givenVirtualCollectionJsonResponse(String path,
      int pageNumber, int pageSize, String userId, String virtualCollectionID, boolean hasNextPage) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    var dataNodes = givenVirtualCollectionJsonApiDataList(pageSize, userId, virtualCollectionID);
    return new JsonApiListResponseWrapper(dataNodes, linksNode, new JsonApiMeta(pageSize));
  }

  public static List<JsonApiData> givenVirtualCollectionJsonApiDataList(int pageSize, String userId,
      String annotationId) {
    return Collections.nCopies(pageSize, new JsonApiData(annotationId, "ods:VirtualCollection",
        MAPPER.valueToTree(givenVirtualCollectionResponse(annotationId, userId))));
  }
}

