package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record MachineAnnotationService(
    @NotBlank String name,
    @NotBlank String containerImage,
    @NotBlank String containerTag,
    JsonNode targetDigitalObjectFilters,
    String serviceDescription,
    String serviceState,
    String sourceCodeRepository,
    String serviceAvailability,
    String codeMaintainer,
    String codeLicense,
    List<String> dependencies,
    String supportContact,
    String slaDocumentation,
    String topicName,
    int maxReplicas,
    @NotBlank boolean batchingRequested) {

}
