package eu.dissco.backend.repository;

public class RepositoryUtils {

  public static final String HANDLE_STRING = "https://hdl.handle.net/";
  public static final String DOI_STRING = "https://doi.org/";
  protected static final int ONE_TO_CHECK_NEXT = 1;

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
