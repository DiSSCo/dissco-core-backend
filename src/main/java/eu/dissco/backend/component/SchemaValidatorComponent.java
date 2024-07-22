package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.schema.AnnotationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaValidatorComponent {

  private final JsonSchema annotationSchema;
  private final ObjectMapper mapper;

  public void validateAnnotationRequest(AnnotationRequest annotationRequest, Boolean isNew)
      throws InvalidAnnotationRequestException {
    validateId(annotationRequest, isNew);
    var annotationRequestNode = mapper.valueToTree(annotationRequest);
    var errors = annotationSchema.validate(annotationRequestNode);
    if (errors.isEmpty()) {
      return;
    }
    log.error("Invalid annotationRequests request. Errors {}", errors);
    throw new InvalidAnnotationRequestException(errors.toString());
  }

  public void validateAnnotationEventRequest(AnnotationEventRequest event, boolean isNew)
      throws InvalidAnnotationRequestException {
    if (event.annotationRequests().size() != 1 || event.batchMetadata().size() != 1
        || event.batchMetadata().get(0).getSearchParams().isEmpty()) {
      var searchParamSize = event.batchMetadata().isEmpty() ? 0
          : event.batchMetadata().get(0).getSearchParams().size();
      log.error(
          "Invalid annotationRequests event: contains {} annotationRequests (1 expected), {} batch metadata (1 expected), and {} searchParams (min 1)",
          event.annotationRequests().size(), event.batchMetadata().size(),
          searchParamSize);
      throw new InvalidAnnotationRequestException(
          "Event can only contain: 1 annotationRequests, 1 batch metadata, and minimum 1 search param");
    }
    validateAnnotationRequest(event.annotationRequests().get(0), isNew);
  }


  void validateId(AnnotationRequest annotation, Boolean isNew)
      throws InvalidAnnotationRequestException {
    if (Boolean.TRUE.equals(isNew) && annotation.getOdsID() != null) {
      throw new InvalidAnnotationRequestException(
          "Attempting overwrite annotationRequests with \"ods:id\" " + annotation.getOdsID());
    }
    if (Boolean.FALSE.equals(isNew) && annotation.getOdsID() == null) {
      throw new InvalidAnnotationRequestException(
          "\"ods:id\" not provided for annotationRequests update");
    }
  }

}
