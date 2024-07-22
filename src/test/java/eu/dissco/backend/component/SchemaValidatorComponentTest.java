package eu.dissco.backend.component;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationEventRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenBatchMetadata;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.schema.AnnotationRequest;
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

  private static Stream<Arguments> validAnnotations() {
    return Stream.of(
        Arguments.of(givenAnnotationRequest(), true),
        Arguments.of(givenAnnotationRequest().withOdsID(HANDLE + ID), false)
    );
  }

  private static Stream<Arguments> invalidAnnotations() {
    return Stream.of(
//        Arguments.of(givenAnnotationRequest().setOaCreator(givenCreator(ORCID)), "oa:creator"),
//        Arguments.of(givenAnnotationRequest().setDcTermsCreated(CREATED), "dcterms:created"),
//        Arguments.of(givenAnnotationRequest().setOaGenerated(CREATED), "oa:generated"),
//        Arguments.of(givenAnnotationRequest().setAsGenerator(givenGenerator()), "as:generator"),
        Arguments.of(givenAnnotationRequest().withOdsID(ID), "ods:id"),
        Arguments.of(givenAnnotationRequest().withOaHasBody(null), "oa:hasBody"),
        Arguments.of(givenAnnotationRequest().withOaHasTarget(null), "oa:hasTarget"),
        Arguments.of(givenAnnotationRequest().withOaMotivation(null), "oa:motivation")
    );
  }

  private static Stream<Arguments> invalidEvents() {
    return Stream.of(
        Arguments.of(new AnnotationEventRequest(Collections.nCopies(2, givenAnnotationRequest()),
            List.of(givenBatchMetadata()))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            Collections.nCopies(2, givenBatchMetadata()))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            List.of(new BatchMetadata(Collections.emptyList())))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            Collections.emptyList()))
    );
  }

  @BeforeEach
  void setup() throws IOException {
    var factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
    try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("json-schema/annotation-request.json")) {
      var schema = factory.getSchema(inputStream);
      schemaValidator = new SchemaValidatorComponent(schema, MAPPER);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidAnnotations")
  void testInvalidAnnotations(AnnotationRequest annotationRequest, String targetIssue) {
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
  void testValidateAnnotationEventRequestInvalid(AnnotationEventRequest event) {
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
  void testValidAnnotation(AnnotationRequest annotationRequest, Boolean isNew) {
    // Then
    assertDoesNotThrow(() ->
        schemaValidator.validateAnnotationRequest(annotationRequest, isNew));
  }
}
