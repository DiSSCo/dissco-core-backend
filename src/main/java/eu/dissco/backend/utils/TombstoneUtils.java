package eu.dissco.backend.utils;

import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.TombstoneMetadata;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class TombstoneUtils {

  private TombstoneUtils() {
    // This is a utility class
  }

  public static TombstoneMetadata buildTombstoneMetadata(Agent agent, String text,
      Instant timestamp) {
    return new TombstoneMetadata()
        .withType("ods:TombstoneMetadata")
        .withOdsHasAgents(List.of(agent))
        .withOdsTombstoneDate(Date.from(timestamp))
        .withOdsTombstoneText(text);
  }
}
