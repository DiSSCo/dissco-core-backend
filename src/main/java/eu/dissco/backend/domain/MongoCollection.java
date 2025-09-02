package eu.dissco.backend.domain;

import lombok.Getter;

@Getter
public enum MongoCollection {

  DIGITAL_SPECIMEN("digital_specimen_provenance", MongoCollection.ODS_VERSION),
  DIGITAL_MEDIA("digital_media_provenance", MongoCollection.ODS_VERSION),
  ANNOTATION("annotation_provenance", MongoCollection.ODS_VERSION),
  VIRTUAL_COLLECTION("virtual_collection_provenance", MongoCollection.SCHEMA_VERSION);

  private final String collectionName;
  private final String versionProperty;
  private static final String ODS_VERSION = "ods:version";
  private static final String SCHEMA_VERSION = "schema:version";
  MongoCollection(String collectionName, String versionProperty) {
    this.collectionName = collectionName;
    this.versionProperty = versionProperty;
  }
}
