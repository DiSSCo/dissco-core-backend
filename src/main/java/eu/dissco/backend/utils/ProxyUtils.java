package eu.dissco.backend.utils;

public class ProxyUtils {

  public static final String HANDLE_PROXY = "https://hdl.handle.net/";
  public static final String DOI_PROXY = "https://doi.org/";

  private ProxyUtils() {
    // Utility class not meant to be instantiated
  }

  public static String removeHandleProxy(String id) {
    return id.replace(HANDLE_PROXY, "");
  }

  public static String removeDoiProxy(String id) {
    return id.replace(DOI_PROXY, "");
  }

  public static String getFullId(String id) {
    return (id.contains(DOI_PROXY)) ? id : DOI_PROXY + id;
  }

}
