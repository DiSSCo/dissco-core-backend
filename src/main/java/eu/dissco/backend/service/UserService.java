package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.InvalidIdException;
import eu.dissco.backend.exceptions.InvalidTypeException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UserExistsException;
import eu.dissco.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private static final String TYPE = "users";

  private final ObjectMapper mapper;
  private final UserRepository repository;

  public JsonApiData createNewUser(JsonApiWrapper request)
      throws JsonProcessingException, ConflictException {
    checkType(request);
    var requestUser = mapper.treeToValue(request.getData().getAttributes(), User.class);
    var id = request.getData().getId();
    var optionalUser = repository.findOptional(id);
    if (optionalUser.isEmpty()) {
      var userResponse = repository.createNewUser(id, requestUser);
      return new JsonApiData(id, TYPE, mapper.valueToTree(userResponse));
    } else {
      log.warn("User with id: {} already exists in the database", id);
      throw new UserExistsException();
    }
  }

  private void checkType(JsonApiWrapper request) throws InvalidTypeException {
    if (!TYPE.equals(request.getData().getType())) {
      log.warn("Type: {} is not relevant for users endpoint", request.getData().getType());
      throw new InvalidTypeException();
    }
  }

  public JsonApiData findUserFromOrcid(String orcid) throws NotFoundException {
    var userOptional = repository.findOptionalFromOrcid(orcid);
    if (userOptional.isPresent()) {
      return new JsonApiData(orcid, TYPE, mapper.valueToTree(userOptional.get()));
    }
    throw new NotFoundException("User with ORCID " + orcid + " does not exist");
  }

  public JsonApiData findUser(String id) throws NotFoundException {
    var userOptional = repository.findOptional(id);
    if (userOptional.isPresent()) {
      return new JsonApiData(id, TYPE, mapper.valueToTree(userOptional.get()));
    }
    throw new NotFoundException("User with id " + id + " does not exist");
  }

  public User getUser(String id) {
    return repository.find(id);
  }

  public JsonApiData updateUser(String id, JsonApiWrapper request)
      throws ConflictException {
    checkType(request);
    if (id.equals(request.getData().getId())) {
      var user = repository.updateUser(id, request.getData().getAttributes());
      return new JsonApiData(id, TYPE, mapper.valueToTree(user));
    } else {
      log.warn("ID: {} is equal to id in request: {}", id, request.getData().getType());
      throw new InvalidIdException();
    }
  }

  public void deleteUser(String id) {
    repository.deleteUser(id);
  }
}
