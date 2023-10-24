package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class HandleUtils {
  private HandleUtils(){}

  public static List<JsonNode> givenPostRequest() throws Exception {
    return List.of(MAPPER.readTree("""
        {
            "data": {
              "type": "handle",
              "attributes": {
               "fdoProfile": "https://hdl.handle.net/21.T11148/64396cf36b976ad08267",
                "issuedForAgent": "https://ror.org/0566bfb96",
                "digitalObjectType": "https://hdl.handle.net/21.T11148/64396cf36b976ad08267"
              }
            }
          }
        """));
  }


}
