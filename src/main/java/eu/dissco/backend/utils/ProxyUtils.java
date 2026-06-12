package eu.dissco.backend.utils;

import com.nimbusds.jose.util.Pair;

public class ProxyUtils {

	public static final String HANDLE_PROXY = "https://hdl.handle.net/";

	public static final String DOI_PROXY = "https://doi.org/";

	private ProxyUtils() {
		// Utility class not meant to be instantiated
	}

	public static String removeHandleProxy(String id) {
		return id.replace(HANDLE_PROXY, "");
	}

	public static Pair<String, String> splitHandle(String handle){
		var splitHandle = handle.split("/");
		if (splitHandle.length == 2){
			return Pair.of(splitHandle[0], splitHandle[1]);
		} else {
		 throw new IllegalArgumentException("Invalid handle format: " + handle);
		}
	}

	public static String removeDoiProxy(String id) {
		return id.replace(DOI_PROXY, "");
	}

	public static String getFullId(String id) {
		return (id.contains(DOI_PROXY)) ? id : DOI_PROXY + id;
	}

}
