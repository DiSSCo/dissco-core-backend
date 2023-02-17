package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.domain.User;
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
    var requestUser = mapper.treeToValue(request.data().attributes(), User.class);
    var id = request.data().id();
    var optionalUser = repository.find(id);
    if (optionalUser.isEmpty()) {
      var userResponse = repository.createNewUser(id, requestUser);
      return new JsonApiData(id, TYPE, mapper.valueToTree(userResponse));
    } else {
      log.warn("User with id: {} already exists in the database", id);
      throw new UserExistsException();
    }
  }

  private void checkType(JsonApiWrapper request) throws InvalidTypeException {
    if (!TYPE.equals(request.data().type())) {
      log.warn("Type: {} is not relevant for users endpoint", request.data().type());
      throw new InvalidTypeException();
    }
  }

  public JsonApiData findUser(String id) throws NotFoundException {
    var optionalUser = repository.find(id);
    if (optionalUser.isPresent()) {
      return new JsonApiData(id, TYPE, mapper.valueToTree(optionalUser.get()));
    } else {
      throw new NotFoundException();
    }
  }

  public JsonApiData updateUser(String id, JsonApiWrapper request)
      throws ConflictException, NotFoundException {
    checkType(request);
    if (id.equals(request.data().id())) {
      var optionalUser = repository.find(id);
      if (optionalUser.isPresent()) {
        var user = repository.updateUser(id, request.data().attributes());
        return new JsonApiData(id, TYPE, mapper.valueToTree(user));
      } else {
        log.warn("No user with id: {} is present in the database", id);
        throw new NotFoundException();
      }
    } else {
      log.warn("ID: {} is equal to id in request: {}", id, request.data().type());
      throw new InvalidIdException();
    }
  }

  public void deleteUser(String id) throws NotFoundException {
    var optionalUser = repository.find(id);
    if (optionalUser.isPresent()) {
      repository.deleteUser(id);
    } else {
      throw new NotFoundException();
    }
  }
}
