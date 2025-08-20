package eu.dissco.backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.MasScheduleJobRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mas", url = "${feign.mas}")
public interface MasClient {

  @PostMapping(value = "/")
  JsonNode scheduleMas(@RequestBody MasScheduleJobRequest masJobRequest);

}
