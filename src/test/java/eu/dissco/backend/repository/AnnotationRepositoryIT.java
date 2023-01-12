package eu.dissco.backend.repository;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.tables.NewAnnotation;
import eu.dissco.backend.domain.AnnotationResponse;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotationRepositoryIT extends BaseRepositoryIT {
  private AnnotationRepository repository;
  private ObjectMapper mapper;

  @BeforeEach
  private void setup(){
    mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new AnnotationRepository(context, mapper);
  }


  private void postAnnotations(List<AnnotationResponse> annotations){
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations){
      var query = context.insertInto(NewAnnotation.NEW_ANNOTATION)
          .set(NewAnnotation.NEW_ANNOTATION.ID, annotation.id())
          .set(NewAnnotation.NEW_ANNOTATION.VERSION, annotation.version())
          .set(NewAnnotation.NEW_ANNOTATION.TYPE, annotation.type())
          .set(NewAnnotation.NEW_ANNOTATION.CREATOR, annotation.creator())
          .set(NewAnnotation.NEW_ANNOTATION.MOTIVATION, annotation.motivation())
          .set(NewAnnotation.NEW_ANNOTATION.TARGET_ID, annotation.target().toString());
    }


  }


}
