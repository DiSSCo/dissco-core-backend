package eu.dissco.backend.utils;


import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;

import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SpecimenUtils {

  public static final String SPECIMEN_URI = "/api/v1/specimens";
  public static final String SPECIMEN_PATH = SANDBOX_URI + SPECIMEN_URI;

  public static List<JsonApiData> givenDigitalSpecimenJsonApiData(
      List<DigitalSpecimen> digitalSpecimenList) {
    List<JsonApiData> dataNode = new ArrayList<>();
    digitalSpecimenList.forEach(specimenWrapper -> dataNode.add(
        new JsonApiData(specimenWrapper.getOdsID(),
            specimenWrapper.getOdsType(), MAPPER.valueToTree(specimenWrapper))));
    return dataNode;
  }

  public static JsonApiData givenDigitalSpecimenJsonApiData(
      DigitalSpecimen specimenWrapper) {
    return new JsonApiData(specimenWrapper.getOdsID(), specimenWrapper.getOdsType(),
        MAPPER.valueToTree(specimenWrapper));
  }

  public static List<DigitalSpecimen> givenDigitalSpecimenList(int qty) {
    List<DigitalSpecimen> digitalSpecimens = new ArrayList<>();
    IntStream.range(0, qty).boxed().toList().forEach(i -> {
      digitalSpecimens.add(givenDigitalSpecimenWrapper(String.valueOf(i)));
    });
    return digitalSpecimens;
  }

  public static List<JsonApiData> givenDigitalSpecimenJsonApiDataList(int qty) {
    List<DigitalSpecimen> digitalSpecimen = givenDigitalSpecimenList(qty);
    List<JsonApiData> dataNode = new ArrayList<>();
    digitalSpecimen.forEach(
        s -> dataNode.add(new JsonApiData(s.getOdsID(), s.getOdsType(), MAPPER.valueToTree(s))));
    return dataNode;
  }

}
