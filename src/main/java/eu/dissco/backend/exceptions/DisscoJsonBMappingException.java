package eu.dissco.backend.exceptions;

import org.springframework.dao.DataAccessException;

public class DisscoJsonBMappingException extends DataAccessException {

  public DisscoJsonBMappingException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
