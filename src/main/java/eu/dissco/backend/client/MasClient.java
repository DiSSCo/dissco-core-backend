package eu.dissco.backend.client;

import eu.dissco.backend.domain.MasScheduleJobRequest;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.JsonNode;

public interface MasClient {

	@PostExchange(value = "/")
	JsonNode scheduleMas(@RequestBody Set<MasScheduleJobRequest> masJobRequest) throws WebProcessingFailedException;

}
