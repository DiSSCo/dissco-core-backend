package eu.dissco.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.Authoritative;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.Image;
import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.domain.OrganisationTuple;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

public class TestUtils {

  public static final String ID = "test/eab36efab0bf0e60dfe0";
  public static final String TYPE = "ODStypeV0.2-Test";
  public static final int MIDS_LEVEL = 1;
  public static final String CURATED_OBJECT_ID = "https://data.biodiversitydata.nl/naturalis/specimen/L.3527331";
  public static final String PHYSICAL_SPECIMEN_ID = "L.3527331@BRAHMS";
  public static final String INSTITUTION = "https://ror.org/0566bfb96";
  public static final String INSTITUTION_CODE = "Naturalis Biodiversity Center";
  public static final String MATERIAL_TYPE = "Herbarium sheet";
  public static final String NAME = "Blechnum occidentale L.";
  public static final String IMAGE_URL = "https://medialib.naturalis.nl/file/id/L.3527331/format/large";
  public static final String ORGANISATION_NAME = "Natural History Museum Vienna";
  public static final String ORGANISATION_ROR = "https://ror.org/01tv5y993";

  public static final String ANNOTATION_ID = "test/7290f662-0cbc-4277-a6a8-01447a2c7b1f";
  public static final String ANNOTATION_TYPE = "Annotation";
  public static final String ANNOTATION_TARGET = "test/a89d7de4-9bbf-4023-bf6c-e4a17176e090";
  public static final String ANNOTATION_CREATOR = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public static final Instant ANNOTATION_LAST_UPDATED = Instant.parse("2022-06-14T11:28:00.00Z");
  public static final String ANNOTATION_BODY = "{\"text\": \"This is an annotation\"}";
  private static final ObjectMapper mapper = new ObjectMapper();

  public static byte[] loadResourceFile(String fileName) throws IOException {
    return new ClassPathResource(fileName).getInputStream().readAllBytes();
  }

  public static DigitalSpecimen givenDigitalSpecimen() {
    var ds = new DigitalSpecimen();
    ds.setId(ID);
    ds.setType(TYPE);
    var authoritative = new Authoritative();
    authoritative.setInstitution(INSTITUTION);
    authoritative.setMaterialType(MATERIAL_TYPE);
    authoritative.setMidslevel(MIDS_LEVEL);
    authoritative.setPhysicalSpecimenId(PHYSICAL_SPECIMEN_ID);
    authoritative.setCuratedObjectID(CURATED_OBJECT_ID);
    authoritative.setInstitutionCode(INSTITUTION_CODE);
    authoritative.setName(NAME);
    ds.setAuthoritative(authoritative);
    var image = new Image();
    image.setImageUri(IMAGE_URL);
    ds.setImages(List.of(image));
    return ds;
  }

  public static OrganisationTuple givenOrganisationTuple() {
    return new OrganisationTuple(ORGANISATION_NAME, ORGANISATION_ROR);
  }

  public static OrganisationDocument givenOrganisationDocument() {
    var document = new OrganisationDocument();
    document.setDocumentId("test/123");
    document.setOrganisationId(ORGANISATION_ROR);
    document.setDocumentType("Google form");
    document.setDocumentTitle("This is a test document");
    document.setDocument(mapper.createObjectNode());
    return document;
  }

  public static AnnotationResponse givenAnnotation() throws JsonProcessingException {
    return new AnnotationResponse(ANNOTATION_ID, ANNOTATION_TYPE, mapper.readTree(ANNOTATION_BODY),
        ANNOTATION_TARGET, ANNOTATION_LAST_UPDATED, ANNOTATION_CREATOR);
  }

  public static AnnotationRequest givenAnnotationRequest() throws JsonProcessingException {
    return new AnnotationRequest(ANNOTATION_ID, ANNOTATION_TYPE, mapper.readTree(ANNOTATION_BODY),
        ANNOTATION_TARGET);
  }
}
