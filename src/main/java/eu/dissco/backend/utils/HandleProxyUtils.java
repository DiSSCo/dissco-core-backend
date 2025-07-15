package eu.dissco.backend.utils;

public class HandleProxyUtils {

  public static final String HANDLE_PROXY = "https://hdl.handle.net/";

  private HandleProxyUtils() {
    // Utility class not meant to be instantiated
  }

  public static String removeProxy(String id) {
    return id.replace(HANDLE_PROXY, "");
  }
}
