package eu.dissco.backend.controller;

import static eu.dissco.backend.util.TestUtils.ANNOTATION_ID;
import static eu.dissco.backend.util.TestUtils.givenAnnotation;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenId;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import eu.dissco.backend.security.KeycloakConfig;
import eu.dissco.backend.security.MethodSecurityConfig;
import eu.dissco.backend.security.WebSecurityConfig;
import eu.dissco.backend.service.AnnotationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = {AnnotationController.class,
    RestResponseEntityExceptionHandler.class})
@ComponentScan(basePackageClasses = {KeycloakSpringBootConfigResolver.class})
@Import({WebSecurityConfig.class, KeycloakConfig.class, MethodSecurityConfig.class})
class AnnotationControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private AnnotationService service;

  @Test
  void testGetAnnotation() throws Exception {
    // Given
    given(service.getAnnotation(ANNOTATION_ID)).willReturn(givenAnnotation());

    // When
    var result = this.mockMvc.perform(
        get("/api/v1/annotations/test/7290f662-0cbc-4277-a6a8-01447a2c7b1f"));

    // Then
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }

}
