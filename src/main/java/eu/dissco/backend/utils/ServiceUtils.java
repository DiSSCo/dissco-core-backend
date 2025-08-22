package eu.dissco.backend.utils;

public class ServiceUtils {

  private ServiceUtils() {
    // Utility class
  }

  public static boolean hasNext(int pageNumber, int pageSize, Long totalResults) {
    var objectsCounted = pageNumber * pageSize;
    return objectsCounted < totalResults;
  }

}
