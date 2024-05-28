package eu.dissco.backend.component;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationEventRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenBatchMetadata;
import static eu.dissco.backend.utils.AnnotationUtils.givenCreator;
import static eu.dissco.backend.utils.AnnotationUtils.givenGenerator;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaValidatorComponentTest {

  private SchemaValidatorComponent schemaValidator;

  @BeforeEach
  void setup() throws IOException {
    var factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
    try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("json-schema/annotation_request.json")) {
      var schema = factory.getSchema(inputStream);
      schemaValidator = new SchemaValidatorComponent(schema, MAPPER);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidAnnotations")
  void testInvalidAnnotations(Annotation annotationRequest, String targetIssue) {
    // When
    var e = assertThrows(InvalidAnnotationRequestException.class,
        () -> schemaValidator.validateAnnotationRequest(annotationRequest,
            true));

    // Then
    assertThat(e.getMessage()).contains(targetIssue);
  }

  @Test
  void testValidateAnnotationEventRequest() {
    // When Then
    assertDoesNotThrow(
        () -> schemaValidator.validateAnnotationEventRequest(givenAnnotationEventRequest(), true));
  }

  @ParameterizedTest
  @MethodSource("invalidEvents")
  void testValidateAnnotationEventRequestInvalid(AnnotationEvent event) {
    // When Then
    assertThrows(InvalidAnnotationRequestException.class,
        () -> schemaValidator.validateAnnotationEventRequest(event, true));
  }

  @Test
  void testUpdateMissingId() {
    // Given
    var annotationRequest = givenAnnotationRequest();

    // When
    var e = assertThrows(InvalidAnnotationRequestException.class,
        () -> schemaValidator.validateAnnotationRequest(annotationRequest,
            false));

    // Then
    assertThat(e.getMessage()).contains("ods:id");
  }

  @ParameterizedTest
  @MethodSource("validAnnotations")
  void testValidAnnotation(Annotation annotationRequest, Boolean isNew) {
    // Then
    assertDoesNotThrow(() ->
        schemaValidator.validateAnnotationRequest(annotationRequest, isNew));
  }

  private static Stream<Arguments> validAnnotations() {
    return Stream.of(
        Arguments.of(givenAnnotationRequest(), true),
        Arguments.of(givenAnnotationRequest().setOdsId(ID), false)
    );
  }

  private static Stream<Arguments> invalidAnnotations() {
    return Stream.of(
        Arguments.of(givenAnnotationRequest().setOaCreator(givenCreator(ORCID)), "oa:creator"),
        Arguments.of(givenAnnotationRequest().setDcTermsCreated(CREATED), "dcterms:created"),
        Arguments.of(givenAnnotationRequest().setOaGenerated(CREATED), "oa:generated"),
        Arguments.of(givenAnnotationRequest().setAsGenerator(givenGenerator()), "as:generator"),
        Arguments.of(givenAnnotationRequest().setOdsId(ID), "ods:id"),
        Arguments.of(givenAnnotationRequest().setOaBody(null), "oa:body"),
        Arguments.of(givenAnnotationRequest().setOaTarget(null), "oa:target"),
        Arguments.of(givenAnnotationRequest().setOaMotivation(null), "oa:motivation")
    );
  }

  private static Stream<Arguments> invalidEvents() {
    return Stream.of(
        Arguments.of(new AnnotationEvent(Collections.nCopies(2, givenAnnotationRequest()),
            List.of(givenBatchMetadata()))),
        Arguments.of(new AnnotationEvent(List.of(givenAnnotationRequest()),
            Collections.nCopies(2, givenBatchMetadata()))),
        Arguments.of(new AnnotationEvent(List.of(givenAnnotationRequest()),
            List.of(new BatchMetadata(Collections.emptyList())))),
        Arguments.of(new AnnotationEvent(List.of(givenAnnotationRequest()),
            Collections.emptyList()))
    );
  }
}
