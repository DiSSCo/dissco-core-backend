package eu.dissco.backend.utils;

import static eu.dissco.backend.schema.Identifier.DctermsType.DOI;
import static eu.dissco.backend.schema.Identifier.OdsGupriLevel.GLOBALLY_UNIQUE_STABLE_PERSISTENT_RESOLVABLE_FDO_COMPLIANT;
import static eu.dissco.backend.schema.Identifier.OdsIdentifierStatus.PREFERRED;

import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.Identifier;
import eu.dissco.backend.schema.Identifier.DctermsType;
import eu.dissco.backend.schema.OdsHasRole;
import java.util.List;

public class AgentUtils {

  public static final String ROLE_NAME_ANNOTATOR = "annotator";
  public static final String ROLE_NAME_VIRTUAL_COLLECTION = "virtual-collection-admin";
  public static final String ORCID = "orcid";

  private AgentUtils() {
  }

  public static Agent createAgent(String name, String pid, String roleName, String idTitle,
      Type agentType) {
    var agent = new Agent()
        .withType(agentType)
        .withId(pid)
        .withSchemaName(name)
        .withSchemaIdentifier(pid)
        .withOdsHasRoles(List.of(new OdsHasRole().withType("schema:Role")
            .withSchemaRoleName(roleName)));
    if (pid != null) {
      var identifier = new Identifier()
          .withType("ods:Identifier")
          .withId(pid)
          .withDctermsIdentifier(pid)
          .withOdsIsPartOfLabel(false)
          .withOdsIdentifierStatus(PREFERRED)
          .withOdsGupriLevel(
              GLOBALLY_UNIQUE_STABLE_PERSISTENT_RESOLVABLE_FDO_COMPLIANT);
      if (DOI.value().equals(idTitle)) {
        identifier.withDctermsType(DOI);
        identifier.withDctermsTitle("DOI");
      } else if (ORCID.equals(idTitle)) {
        identifier.withDctermsType(DctermsType.URL);
        identifier.withDctermsTitle("ORCID");
      }
      agent.setOdsHasIdentifiers(List.of(identifier));
    }
    return agent;
  }
}
