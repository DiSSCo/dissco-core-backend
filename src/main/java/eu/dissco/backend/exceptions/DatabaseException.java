package eu.dissco.backend.exceptions;

import org.springframework.dao.DataAccessException;

public class DatabaseException extends DataAccessException {
  public DatabaseException(String s){
    super(s);
  }

}
