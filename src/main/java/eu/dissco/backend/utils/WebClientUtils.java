package eu.dissco.backend.utils;

import eu.dissco.backend.exceptions.WebProcessingFailedException;
import reactor.core.publisher.Mono;

public class WebClientUtils {

	private WebClientUtils() {

	}

	public static <T> T blockAndUnwrap(Mono<T> mono) throws WebProcessingFailedException {
		try {
			return mono.block();
		}
		catch (Exception ex) {
			if (ex.getCause() != null && ex.getCause() instanceof WebProcessingFailedException wpfe) {
				throw wpfe;
			}
			throw new WebProcessingFailedException(ex.getLocalizedMessage());
		}
	}

}
