package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpecimenServiceTest {

  @Mock
  private SpecimenRepository repository;
  @Mock
  private ElasticSearchRepository elasticRepository;
  @Mock
  private DigitalMediaObjectService digitalMediaObjectService;
  @Mock
  private AnnotationService annotationService;
  @Mock
  private MongoRepository mongoRepository;

  private SpecimenService service;

  @BeforeEach
  void setup() {
    service = new SpecimenService(MAPPER, repository, elasticRepository, digitalMediaObjectService,
        annotationService, mongoRepository);
  }

  @Test
  void testGetSpecimen() throws JsonProcessingException {
    // Given
    var digitalSpecimens = List.of(givenDigitalSpecimen(ID));
    given(repository.getSpecimensLatest(1, 10)).willReturn(digitalSpecimens);

    // When
    var result = service.getSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(digitalSpecimens);
  }

  @Test
  void testGetSpecimenById() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    given(repository.getLatestSpecimenById(ID)).willReturn(digitalSpecimen);

    // When
    var result = service.getSpecimenById(ID);

    // Then
    assertThat(result).isEqualTo(digitalSpecimen);
  }

  @Test
  void testSearch() throws IOException {
    // Given
    var digitalSpecimens = List.of(givenDigitalSpecimen(ID));
    var query = "Leucanthemum ircutianum";
    given(elasticRepository.search(query, 1, 10)).willReturn(digitalSpecimens);

    // When
    var result = service.search(query, 1, 10);

    // Then
    assertThat(result).isEqualTo(digitalSpecimens);
  }

  @Test
  void testGetAnnotations() {
    // Given
    var annotations = List.of(givenAnnotationResponse());
    given(annotationService.getAnnotationForTarget(ID)).willReturn(annotations);

    // When
    var result = service.getAnnotations(ID);

    // Then
    assertThat(result).isEqualTo(annotations);
  }

  @Test
  void testSpecimenByVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 4;
    var responseExpected = givenMongoDBMediaResponse();
    given(mongoRepository.getByVersion(ID, version, "digital_specimen_provenance")).willReturn(
        responseExpected);

    // When
    var responseReceived = service.getSpecimenByVersion(ID, version);

    // Then
    assertThat(responseReceived).isEqualTo(givenDigitalSpecimen(ID, version));
  }

  @Test
  void testGetSpecimenVersions() throws NotFoundException {
    // Given
    List<Integer> responseExpected = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "digital_specimen_provenance")).willReturn(
        responseExpected);

    // When
    var responseReceived = service.getSpecimenVersions(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetSpecimenByIdFull() throws JsonProcessingException {
    // Given
    var specimen = givenDigitalSpecimen(ID);
    var digitalMedia = List.of(new DigitalMediaObjectFull(givenDigitalMediaObject(ID), List.of()));
    var annotations = List.of(givenAnnotationResponse(USER_ID_TOKEN, ID));
    given(repository.getLatestSpecimenById(ID)).willReturn(specimen);
    given(digitalMediaObjectService.getDigitalMediaObjectFull(ID)).willReturn(digitalMedia);
    given(annotationService.getAnnotationForTarget(ID)).willReturn(annotations);
    var expected = new DigitalSpecimenFull(specimen, digitalMedia, annotations);

    // When
    var result = service.getSpecimenByIdFull(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetDigitalMedia() {
    // Given
    var digitalMedia = List.of(givenDigitalMediaObject(ID));
    given(digitalMediaObjectService.getDigitalMediaForSpecimen(ID)).willReturn(digitalMedia);

    // When
    var result = service.getDigitalMedia(ID);

    // Then
    assertThat(result).isEqualTo(digitalMedia);
  }

  @Test
  void testGetLatestSpecimen() throws IOException {
    // Given
    var specimens = List.of(givenDigitalSpecimen(ID));
    given(elasticRepository.getLatestSpecimen(1, 10)).willReturn(specimens);

    // When
    var result = service.getLatestSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(specimens);
  }

  @Test
  void testSpecimenByIdJsonLD() throws IOException {
    // Given
    var specimens = givenDigitalSpecimen(ID);
    given(repository.getLatestSpecimenById(ID)).willReturn(specimens);
    given(digitalMediaObjectService.getDigitalMediaIdsForSpecimen(ID)).willReturn(
        List.of(PREFIX + "XXX-XXX-YYY"));

    // When
    var result = service.getSpecimenByIdJsonLD(ID);

    // Then
    assertThat(MAPPER.valueToTree(result).toPrettyString()).isEqualTo(givenJsonLDString());
  }

  private String givenJsonLDString() {
    return """
        {
          "@id" : "hdl:20.5000.1025/ABC-123-XYZ",
          "@type" : "BotanySpecimen",
          "@context" : {
            "ods:organizationId" : {
              "@type" : "@id"
            },
            "ods:sourceSystemId" : {
              "@type" : "@id"
            },
            "ods:hasSpecimenMedia" : {
              "@container" : "@list",
              "@type" : "@id"
            },
            "hdl" : "https://hdl.handle.net/",
            "ods" : "http://github.com/DiSSCo/openDS/ods-ontology/terms/",
            "dwca" : "http://rs.tdwg.org/dwc/text/",
            "dcterms" : "http://purl.org/dc/terms/"
          },
          "ods:primarySpecimenData" : {
            "ods:midsLevel" : 1,
            "ods:version" : 1,
            "ods:physicalSpecimenId" : "123",
            "ods:physicalSpecimenIdType" : "cetaf",
            "ods:specimenName" : "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
            "ods:organizationId" : "https://ror.org/0349vqz63",
            "ods:datasetId" : "Royal Botanic Garden Edinburgh Herbarium",
            "ods:physicalSpecimenCollection" : "http://biocol.org/urn:lsid:biocol.org:col:15670",
            "dwca:id" : "http://data.rbge.org.uk/herb/E00586417",
            "ods:modified" : "03/12/2012",
            "ods:objectType" : "",
            "dcterms:license" : "http://creativecommons.org/licenses/by/4.0/legalcode",
            "ods:sourceSystemId" : "20.5000.1025/3XA-8PT-SAY"
          },
          "ods:sourceSystemId" : "hdl:20.5000.1025/3XA-8PT-SAY",
          "ods:hasSpecimenMedia" : [ "hdl:20.5000.1025XXX-XXX-YYY" ]
        }""";
  }


  private JsonNode givenMongoDBMediaResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """
                 {
                "id": "20.5000.1025/ABC-123-XYZ",
                "midsLevel": 1,
                "version": 4,
                "created": 1667296764,
                "digitalSpecimen": {
                  "ods:physicalSpecimenId": "123",
                  "ods:type": "BotanySpecimen",
                  "ods:attributes": {
                    "dwca:id": "http://data.rbge.org.uk/herb/E00586417",
                    "ods:modified": "03/12/2012",
                    "ods:datasetId": "Royal Botanic Garden Edinburgh Herbarium",
                    "ods:objectType": "",
                    "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
                    "ods:specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
                    "ods:organizationId": "https://ror.org/0349vqz63",
                    "ods:sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
                    "ods:physicalSpecimenIdType": "cetaf",
                    "ods:physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670"
                  },
                  "ods:originalAttributes": {
                    "dwc:class": "Malacostraca",
                    "dwc:genus": "Mesuca",
                    "dwc:order": "Decapoda",
                    "dwc:family": "Ocypodidae",
                    "dwc:phylum": "Arthropoda",
                    "dwc:country": "Nicobar Islands",
                    "dwc:locality": "Harbour",
                    "dwc:continent": "Eastern Indian Ocean",
                    "dwc:eventDate": "01/01/1846",
                    "dwc:recordedBy": "Rosen",
                    "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
                    "dwc:datasetName": "Natural History Museum Denmark Invertebrate Zoology",
                    "dcterms:modified": "03/12/2012",
                    "dwc:occurrenceID": "debe5b20-e945-40e8-8a55-6d92391ff495",
                    "dwc:preparations": "various - 1",
                    "dwc:basisOfRecord": "PreservedSpecimen",
                    "dwc:catalogNumber": "NHMD79044",
                    "dwc:institutionID": "http://grbio.org/cool/mci8-ehqk",
                    "dwc:collectionCode": "IV",
                    "dwc:higherGeography": "Eastern Indian Ocean, Nicobar Islands",
                    "dwc:institutionCode": "NHMD",
                    "dwc:specificEpithet": "dussumieri",
                    "dwc:acceptedNameUsage": "Mesuca dussumieri",
                    "dwc:otherCatalogNumbers": "CRU-001196"
                  }
                }
              }
                        """, JsonNode.class
    );
  }


}
