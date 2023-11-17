package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import eu.dissco.backend.domain.annotation.Annotation;
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
    throw new InvalidAnnotationRequestException(errors.toString());
  }

  void validateId(Annotation annotation, Boolean isNew) throws InvalidAnnotationRequestException {
    if (Boolean.TRUE.equals(isNew) && annotation.getOdsId() != null) {
      throw new InvalidAnnotationRequestException(
          "Attempting overwrite annotation with \"ods:id\" " + annotation.getOdsId());
    }
    if (Boolean.FALSE.equals(isNew) && annotation.getOdsId() == null) {
      throw new InvalidAnnotationRequestException("\"ods:id\" not provided for annotation update");
    }
  }

}
