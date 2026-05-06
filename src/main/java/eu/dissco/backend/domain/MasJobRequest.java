package eu.dissco.backend.domain;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema
public record MasJobRequest(@Parameter(description = "ID of the MAS") String masId,
		@Parameter(description = "If MAS should be run as a batch") Boolean batching) {

	public MasJobRequest(String masId, Boolean batching) {
		this.masId = masId;
		this.batching = Objects.requireNonNullElse(batching, Boolean.FALSE);
	}

}
