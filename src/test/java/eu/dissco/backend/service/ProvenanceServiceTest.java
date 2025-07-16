package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.APP_HANDLE;
import static eu.dissco.backend.TestUtils.APP_NAME;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.UPDATED;
import static eu.dissco.backend.TestUtils.USER_NAME;
import static eu.dissco.backend.TestUtils.givenAgent;
import static eu.dissco.backend.TestUtils.givenTombstoneMetadata;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_NAME;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTombstoneVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.Identifier.DctermsType;
import eu.dissco.backend.schema.OdsChangeValue;
import eu.dissco.backend.schema.VirtualCollection.OdsStatus;
import eu.dissco.backend.utils.AgentUtils;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvenanceServiceTest {

  @Mock
  private ApplicationProperties properties;
  private ProvenanceService service;

  private static List<Agent> givenExpectedAgents() {
    return List.of(
        AgentUtils.createAgent(USER_NAME, ORCID, "creator",
            "orcid", Type.PROV_PERSON),
        AgentUtils.createAgent(APP_NAME, APP_HANDLE, "dissco-backend",
            DctermsType.DOI.value(), Type.PROV_SOFTWARE_AGENT)
    );
  }

  private static List<OdsChangeValue> givenChangeValueTombstone() {
    return List.of(
        givenOdsChangeValue("add", "/ods:hasTombstoneMetadata", givenTombstoneMetadata()),
        givenOdsChangeValue("replace", "/ods:status", OdsStatus.TOMBSTONE),
        givenOdsChangeValue("replace", "/schema:version", 2),
        givenOdsChangeValue("replace", "/schema:dateModified", Date.from(UPDATED))
    );
  }

  private static List<OdsChangeValue> givenChangeValueUpdate() {
    return List.of(
        givenOdsChangeValue("replace", "/ltc:collectionName", VIRTUAL_COLLECTION_NAME),
        givenOdsChangeValue("replace", "/schema:version", 2)
    );
  }

  private static OdsChangeValue givenOdsChangeValue(String op, String path, Object value) {
    return new OdsChangeValue()
        .withAdditionalProperty("op", op)
        .withAdditionalProperty("path", path)
        .withAdditionalProperty("value", MAPPER.convertValue(value, new TypeReference<>() {
        }));
  }

  @BeforeEach
  void setup() {
    this.service = new ProvenanceService(MAPPER, properties);
  }

  @Test
  void testGenerateCreateEvent() {
    // Given
    given(properties.getName()).willReturn(APP_NAME);
    given(properties.getPid()).willReturn(APP_HANDLE);
    var virtualCollection = givenVirtualCollection(HANDLE + ID);

    // When
    var event = service.generateCreateEvent(MAPPER.valueToTree(virtualCollection), givenAgent());

    // Then
    assertThat(event.getDctermsIdentifier()).isEqualTo(HANDLE + ID + "/" + "1");
    assertThat(event.getProvActivity().getOdsChangeValue()).isNull();
    assertThat(event.getProvActivity().getRdfsComment()).isEqualTo("Object newly created");
    assertThat(event.getProvEntity().getProvValue()).isNotNull();
    assertThat(event.getOdsHasAgents()).isEqualTo(givenExpectedAgents());
  }

  @Test
  void testGenerateUpdateEvent() {
    // Given
    given(properties.getName()).willReturn(APP_NAME);
    given(properties.getPid()).willReturn(APP_HANDLE);
    var virtualCollection = givenVirtualCollection(HANDLE + ID, ORCID, VIRTUAL_COLLECTION_NAME, 2);
    var previousVirtualCollection = givenVirtualCollection(HANDLE + ID, ORCID, "An old name", 1);

    // When
    var event = service.generateUpdateEvent(MAPPER.valueToTree(virtualCollection),
        MAPPER.valueToTree(previousVirtualCollection), givenAgent());

    // Then
    assertThat(event.getDctermsIdentifier()).isEqualTo(HANDLE + ID + "/" + "2");
    assertThat(event.getProvActivity().getRdfsComment()).isEqualTo("Object updated");
    assertThat(event.getProvActivity().getOdsChangeValue()).isEqualTo(givenChangeValueUpdate());
    assertThat(event.getProvEntity().getProvValue()).isNotNull();
    assertThat(event.getOdsHasAgents()).isEqualTo(givenExpectedAgents());
  }

  @Test
  void testGenerateTombstoneEventVirtualCollection() {
    // Given
    given(properties.getName()).willReturn(APP_NAME);
    given(properties.getPid()).willReturn(APP_HANDLE);
    var originalMas = MAPPER.valueToTree(givenVirtualCollection(HANDLE + ID));
    var tombstoneMas = MAPPER.valueToTree(givenTombstoneVirtualCollection());

    // When
    var event = service.generateTombstoneEvent(tombstoneMas, originalMas, givenAgent());

    // Then
    assertThat(event.getDctermsIdentifier()).isEqualTo(HANDLE + ID + "/" + "2");
    assertThat(event.getProvActivity().getOdsChangeValue()).containsExactlyInAnyOrderElementsOf(
        givenChangeValueTombstone());
    assertThat(event.getProvEntity().getProvValue()).isNotNull();
    assertThat(event.getProvActivity().getRdfsComment()).isEqualTo("Object tombstoned");
    assertThat(event.getOdsHasAgents()).isEqualTo(givenExpectedAgents());
  }
}
