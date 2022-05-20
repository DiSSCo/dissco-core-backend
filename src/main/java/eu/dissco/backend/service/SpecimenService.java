package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.repository.CordraRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.CordraObject;
import net.cnri.cordra.api.SearchResults;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

  private final CordraRepository repository;
  private final ObjectMapper mapper;

  public List<DigitalSpecimen> getSpecimen(int pageNumber, int pageSize)
      throws CordraException, JsonProcessingException {
    var specimen = repository.getSpecimen(pageNumber, pageSize);
    return mapToList(new ArrayList<>(pageSize), specimen);
  }

  private List<DigitalSpecimen> mapToList(ArrayList<DigitalSpecimen> result, SearchResults<CordraObject> specimen)
      throws JsonProcessingException {
    for (var object : specimen) {
      var opends = mapToOpenDS(object);
      result.add(opends);
    }
    return result;
  }

  private DigitalSpecimen mapToOpenDS(CordraObject cordraObject)
      throws JsonProcessingException {
    var openDS = mapper.readValue(cordraObject.getContentAsString(), DigitalSpecimen.class);
    openDS.setId(cordraObject.id);
    return openDS;
  }

  public DigitalSpecimen getSpecimenById(String id) throws CordraException, JsonProcessingException {
    var object = repository.getSpecimenById(id);
    return mapToOpenDS(object);
  }

  public List<DigitalSpecimen> search(String query, int pageNumber, int pageSize)
      throws CordraException, JsonProcessingException {
    var specimen = repository.search(query, pageNumber, pageSize);
    return mapToList(new ArrayList<>(), specimen);
  }
}
