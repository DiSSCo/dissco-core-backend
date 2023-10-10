package eu.dissco.backend.utils;


import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.TestUtils;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SpecimenUtils {

  public static final String SPECIMEN_URI = "/api/v1/specimens";
  public static final String SPECIMEN_PATH = SANDBOX_URI + SPECIMEN_URI;

  public static List<JsonApiData> givenDigitalSpecimenJsonApiData(
      List<DigitalSpecimenWrapper> digitalSpecimenList) {
    List<JsonApiData> dataNode = new ArrayList<>();
    digitalSpecimenList.forEach(specimenWrapper -> dataNode.add(
        new JsonApiData(specimenWrapper.digitalSpecimen().getOdsId(),
            specimenWrapper.digitalSpecimen()
                .getOdsType(), MAPPER.valueToTree(specimenWrapper))));
    return dataNode;
  }

  public static JsonApiData givenDigitalSpecimenJsonApiData(
      DigitalSpecimenWrapper specimenWrapper) {
    return new JsonApiData(specimenWrapper.digitalSpecimen().getOdsId(),
        specimenWrapper.digitalSpecimen()
            .getOdsType(), MAPPER.valueToTree(specimenWrapper));
  }

  public static List<DigitalSpecimenWrapper> givenDigitalSpecimenList(int qty) {
    List<DigitalSpecimenWrapper> digitalSpecimens = new ArrayList<>();
    IntStream.range(0, qty).boxed().toList().forEach(i -> {
      try {
        digitalSpecimens.add(TestUtils.givenDigitalSpecimenWrapper(String.valueOf(i)));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return digitalSpecimens;
  }

  public static List<JsonApiData> givenDigitalSpecimenJsonApiDataList(int qty) {
    List<DigitalSpecimenWrapper> digitalSpecimen = givenDigitalSpecimenList(qty);
    List<JsonApiData> dataNode = new ArrayList<>();

    digitalSpecimen.forEach(
        s -> dataNode.add(new JsonApiData(s.digitalSpecimen().getOdsId(), s.digitalSpecimen()
            .getOdsType(), MAPPER.valueToTree(s))));
    return dataNode;
  }

}
