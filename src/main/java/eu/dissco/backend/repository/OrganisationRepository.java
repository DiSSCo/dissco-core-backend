package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrganisationRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<JsonApiData> getOrganisations() {
    return context.select(ORGANISATION_DO.ID, ORGANISATION_DO.ORGANISATION_NAME)
        .from(ORGANISATION_DO).fetch(this::mapToOrganisation);
  }

  private JsonApiData mapToOrganisation(Record dbRecord) {
    ObjectNode attributeNode = mapper.createObjectNode();
    attributeNode.put("organisationName",dbRecord.get(ORGANISATION_DO.ORGANISATION_NAME));
    return new JsonApiData(
        dbRecord.get(ORGANISATION_DO.ID),
        "organisation",
        attributeNode);
  }

  public List<JsonApiData> getCountries() {
    return context.selectDistinct(ORGANISATION_DO.COUNTRY, ORGANISATION_DO.COUNTRY_CODE).from(ORGANISATION_DO).groupBy(ORGANISATION_DO.COUNTRY,
        ORGANISATION_DO.COUNTRY_CODE).fetch(this::mapCountry);
  }


  private JsonApiData mapCountry(Record dbRecord) {
    ObjectNode attributeNode = mapper.createObjectNode();
    attributeNode.put("country", dbRecord.get(ORGANISATION_DO.COUNTRY));
    return new JsonApiData(
        dbRecord.get(ORGANISATION_DO.COUNTRY_CODE),
        "country",
        attributeNode
    );
  }
}
