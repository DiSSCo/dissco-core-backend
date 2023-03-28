package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;

import eu.dissco.backend.domain.OrganisationDocument;

public class OrganisationDocumentTestUtils {
  private OrganisationDocumentTestUtils(){}


  public static OrganisationDocument givenOrganisationDocument(){
    var doc = new OrganisationDocument();
    doc.setOrganisationId(ID);
    doc.setDocumentId(ID_ALT);
    doc.setDocumentTitle("Doc Title");
    doc.setDocumentType("Doc type");
    doc.setDocument(MAPPER.createObjectNode());
    return doc;
  }

}
