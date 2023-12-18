package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.List;

public class HandleUtils {

  public static List<JsonNode> givenPostHandleRequest(int n) throws Exception {
    var baseNode = MAPPER.readTree("""
        {
          "data" : {
            "type" : "handle",
            "attributes" : {
              "fdoProfile" : "https://hdl.handle.net/21.T11148/64396cf36b976ad08267",
              "digitalObjectType":"https://hdl.handle.net/21.T11148/64396cf36b976ad08267",
              "issuedForAgent" : "https://ror.org/0566bfb96"
            }
          }
        }
        """);

    return Collections.nCopies(n, baseNode);
  }

  public static JsonNode givenPostHandleResponse(int n) throws Exception {
    var baseNode = MAPPER.readTree("""
                {
                    "id": "20.5000.1025/3XA-8PT-SAY",
                    "type": "handle",
                    "attributes": {
                        "10320/loc": "<locations/>",
                        "fdoProfile": "https://hdl.handle.net/21.T11148/64396cf36b976ad08267",
                        "fdoRecordLicense": "https://creativecommons.org/publicdomain/zero/1.0/",
                        "digitalObjectType": "https://hdl.handle.net/21.T11148/64396cf36b976ad08267",
                        "digitalObjectName": "FDO-test-basic-type",
                        "pid": "https://hdl.handle.net/TEST/Q4B-Y1C-DSR",
                        "pidIssuer": "https://ror.org/04wxnsj81",
                        "pidIssuerName": "DataCite",
                        "issuedForAgent": "https://ror.org/0566bfb96",
                        "issuedForAgentName": "Naturalis Biodiversity Center",
                        "pidRecordIssueDate": "2023-12-18T13:40:17.983Z",
                        "pidRecordIssueNumber": "1",
                        "structuralType": "digital",
                        "pidStatus": "TEST"
                    },
                    "links": {
                        "self": "https://hdl.handle.net/TEST/Q4B-Y1C-DSR"
                    }
                }
        """);
    var list = Collections.nCopies(n, baseNode);
    var dataNode = MAPPER.createObjectNode().putArray("data").addAll(list);
    return MAPPER.createObjectNode().set("data", dataNode);
  }

}
