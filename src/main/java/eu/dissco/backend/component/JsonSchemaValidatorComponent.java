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
public class JsonSchemaValidatorComponent {

  private final JsonSchema annotationSchema;
  private final ObjectMapper mapper;

  public void validateAnnotationRequest(Annotation annotation)
      throws InvalidAnnotationRequestException {
    var annotationRequest = mapper.valueToTree(annotation);
    var errors = annotationSchema.validate(annotationRequest);
    if (errors.isEmpty()){
      return;
    }
    throw new InvalidAnnotationRequestException(errors.toString());
  }

}
