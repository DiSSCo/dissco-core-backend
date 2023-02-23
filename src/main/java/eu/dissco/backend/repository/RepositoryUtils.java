package eu.dissco.backend.repository;

public class RepositoryUtils {

  private RepositoryUtils() {
    // Utility class
  }

  protected static int getOffset(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return offset;
  }

}
