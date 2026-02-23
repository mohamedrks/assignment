package com.fulfilment.application.monolith.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionMapperTest {

  private GlobalExceptionMapper mapper;

  @BeforeEach
  void setUp() throws Exception {
    mapper = new GlobalExceptionMapper();
    var field = GlobalExceptionMapper.class.getDeclaredField("objectMapper");
    field.setAccessible(true);
    field.set(mapper, new ObjectMapper());
  }

  @Test
  void shouldReturn500ForGenericException() {
    Response response = mapper.toResponse(new RuntimeException("something went wrong"));
    assertEquals(500, response.getStatus());
  }

  @Test
  void shouldIncludeExceptionTypeInBody() {
    Response response = mapper.toResponse(new RuntimeException("oops"));
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(RuntimeException.class.getName(), body.get("exceptionType").asText());
  }

  @Test
  void shouldIncludeCodeInBody() {
    Response response = mapper.toResponse(new RuntimeException("oops"));
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(500, body.get("code").asInt());
  }

  @Test
  void shouldIncludeErrorMessageWhenPresent() {
    Response response = mapper.toResponse(new RuntimeException("error detail"));
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals("error detail", body.get("error").asText());
  }

  @Test
  void shouldNotIncludeErrorFieldWhenMessageIsNull() {
    Response response = mapper.toResponse(new RuntimeException((String) null));
    ObjectNode body = (ObjectNode) response.getEntity();
    assertNull(body.get("error"));
  }

  @Test
  void shouldReturn400ForWebApplicationException400() {
    Response response = mapper.toResponse(new WebApplicationException("bad request", 400));
    assertEquals(400, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(400, body.get("code").asInt());
  }

  @Test
  void shouldReturn404ForWebApplicationException404() {
    Response response = mapper.toResponse(new WebApplicationException("not found", 404));
    assertEquals(404, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(404, body.get("code").asInt());
  }

  @Test
  void shouldReturn422ForWebApplicationException422() {
    Response response = mapper.toResponse(new WebApplicationException("unprocessable", 422));
    assertEquals(422, response.getStatus());
  }
}
