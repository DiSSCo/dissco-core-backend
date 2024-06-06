package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaValidatorComponent {

  private final JsonSchema annotationSchema;
  private final ObjectMapper mapper;

  public void validateAnnotationRequest(Annotation annotation, Boolean isNew)
      throws InvalidAnnotationRequestException {
    validateId(annotation, isNew);
    var annotationRequest = mapper.valueToTree(annotation);
    var errors = annotationSchema.validate(annotationRequest);
    if (errors.isEmpty()) {
      return;
    }
    log.error("Invalid annotations request. Errors {}", errors);
    throw new InvalidAnnotationRequestException(errors.toString());
  }

  public void validateAnnotationEventRequest(AnnotationEvent event, boolean isNew)
      throws InvalidAnnotationRequestException {
    if (event.annotations().size() != 1 || event.batchMetadata().size() != 1
        || event.batchMetadata().get(0).getSearchParams().isEmpty()) {
      var searchParamSize = event.batchMetadata().isEmpty() ? 0
          : event.batchMetadata().get(0).getSearchParams().size();
      log.error(
          "Invalid annotations event: contains {} annotations (1 expected), {} batch metadata (1 expected), and {} searchParams (min 1)",
          event.annotations().size(), event.batchMetadata().size(),
          searchParamSize);
      throw new InvalidAnnotationRequestException(
          "Event can only contain: 1 annotations, 1 batch metadata, and minimum 1 search param");
    }
    validateAnnotationRequest(event.annotations().get(0), isNew);
  }


  void validateId(Annotation annotation, Boolean isNew) throws InvalidAnnotationRequestException {
    if (Boolean.TRUE.equals(isNew) && annotation.getOdsId() != null) {
      throw new InvalidAnnotationRequestException(
          "Attempting overwrite annotations with \"ods:id\" " + annotation.getOdsId());
    }
    if (Boolean.FALSE.equals(isNew) && annotation.getOdsId() == null) {
      throw new InvalidAnnotationRequestException("\"ods:id\" not provided for annotations update");
    }
  }

}
