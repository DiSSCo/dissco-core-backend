package eu.dissco.backend.domain;

import static eu.dissco.backend.repository.MongoRepository.ODS_VERSION;
import static eu.dissco.backend.repository.MongoRepository.SCHEMA_VERSION;

import lombok.Getter;

@Getter
public enum MongoCollection {

  DIGITAL_SPECIMEN("digital_specimen_provenance", ODS_VERSION),
  DIGITAL_MEDIA("digital_media_provenance", ODS_VERSION),
  ANNOTATION("annotation_provenance", ODS_VERSION),
  VIRTUAL_COLLECTION("virtual_collection_provenance", SCHEMA_VERSION);

  private final String collectionName;
  private final String versionProperty;
  MongoCollection(String collectionName, String versionProperty) {
    this.collectionName = collectionName;
    this.versionProperty = versionProperty;
  }
}
