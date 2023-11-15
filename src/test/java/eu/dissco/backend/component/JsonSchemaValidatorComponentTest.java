package eu.dissco.backend.component;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenCreator;
import static eu.dissco.backend.utils.AnnotationUtils.givenGenerator;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Target;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidatorComponentTest {

  private JsonSchemaValidatorComponent validatorComponent;

  @BeforeEach
  void setup() throws IOException {
    var factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
    try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("json-schema/annotation_request.json")) {
      var schema = factory.getSchema(inputStream);
      validatorComponent = new JsonSchemaValidatorComponent(schema, MAPPER);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidAnnotations")
  void testInvalidAnnotations(Annotation annotationRequest, String targetIssue) {
    // When
    var e = assertThrows(InvalidAnnotationRequestException.class,
        () -> validatorComponent.validateAnnotationRequest(annotationRequest,
            true));

    // Then
    assertThat(e.getMessage()).contains(targetIssue);
  }

  @Test
  void testUpdateMissingId() {
    // Given
    var annotationRequest = givenAnnotationRequest();

    // When
    var e = assertThrows(InvalidAnnotationRequestException.class,
        () -> validatorComponent.validateAnnotationRequest(annotationRequest,
            false));

    // Then
    assertThat(e.getMessage()).contains("ods:id");
  }

  @ParameterizedTest
  @MethodSource("validAnnotations")
  void testValidAnnotation(Annotation annotationRequest, Boolean isNew) {
    // Then
    assertDoesNotThrow(() ->
        validatorComponent.validateAnnotationRequest(annotationRequest, isNew));
  }

  private static Stream<Arguments> validAnnotations(){
    return Stream.of(
        Arguments.of(givenAnnotationRequest(), true),
        Arguments.of(givenAnnotationRequest().withOdsId(ID), false)
    );
  }

  private static Stream<Arguments> invalidAnnotations() {
    return Stream.of(
        Arguments.of(givenAnnotationRequest().withOaCreator(givenCreator(ORCID)), "oa:creator"),
        Arguments.of(givenAnnotationRequest().withDcTermsCreated(CREATED), "dcterms:created"),
        Arguments.of(givenAnnotationRequest().withOaGenerated(CREATED), "oa:generated"),
        Arguments.of(givenAnnotationRequest().withAsGenerator(givenGenerator()), "as:generator"),
        Arguments.of(givenAnnotationRequest().withOdsId(ID), "ods:id"),
        Arguments.of(givenAnnotationRequest().withRdfType(null), "rdf:type"),
        Arguments.of(givenAnnotationRequest().withOaBody(null), "oa:body"),
        Arguments.of(givenAnnotationRequest().withOaTarget(null), "oa:target"),
        Arguments.of(givenAnnotationRequest().withOaMotivation(null), "oa:motivation")
    );
  }

}
