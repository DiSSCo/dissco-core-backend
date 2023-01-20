package eu.dissco.backend.controller;

import eu.dissco.backend.service.DigitalMediaObjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ControllerTest {
  @Mock
  private DigitalMediaObjectService service;
  private DigitalMediaObjectController controller;

  @BeforeEach
  void setup(){
    controller = new DigitalMediaObjectController(service);
  }

  void testGetDigitalMediaObjectsNameJsonResponse(){
    int pageSize = 1;
    int pageNum = 1;



  }



}
