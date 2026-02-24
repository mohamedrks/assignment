package com.fulfilment.application.monolith.products;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductResourceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductResource resource;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // --- Product model ---

  @Test
  void productDefaultConstructorShouldCreateEmptyProduct() {
    Product p = new Product();
    assertNull(p.id);
    assertNull(p.name);
  }

  @Test
  void productNameConstructorShouldSetName() {
    Product p = new Product("KALLAX");
    assertEquals("KALLAX", p.name);
  }

  // --- get ---

  @Test
  void getShouldReturnAllProducts() {
    Product p1 = new Product("KALLAX");
    Product p2 = new Product("BESTÅ");
    when(productRepository.listAll(any())).thenReturn(List.of(p1, p2));

    List<Product> result = resource.get();

    assertEquals(2, result.size());
  }

  // --- getSingle ---

  @Test
  void getSingleShouldReturnProductWhenFound() {
    Product product = new Product("KALLAX");
    product.id = 1L;
    when(productRepository.findById(1L)).thenReturn(product);

    Product result = resource.getSingle(1L);

    assertEquals("KALLAX", result.name);
  }

  @Test
  void getSingleShouldThrow404WhenNotFound() {
    when(productRepository.findById(99L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.getSingle(99L));
    assertEquals(404, ex.getResponse().getStatus());
  }

  // --- create ---

  @Test
  void createShouldPersistAndReturn201() {
    Product product = new Product("TONSTAD");

    var response = resource.create(product);

    verify(productRepository).persist(product);
    assertEquals(201, response.getStatus());
  }

  @Test
  void createShouldThrow422WhenIdIsSet() {
    Product product = new Product("TONSTAD");
    product.id = 1L;

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.create(product));
    assertEquals(422, ex.getResponse().getStatus());
  }

  // --- update ---

  @Test
  void updateShouldModifyAndReturnProduct() {
    Product existing = new Product("KALLAX");
    existing.id = 1L;
    existing.price = BigDecimal.valueOf(49.99);

    Product updated = new Product("KALLAX-V2");
    updated.description = "Updated";
    updated.price = BigDecimal.valueOf(59.99);
    updated.stock = 5;

    when(productRepository.findById(1L)).thenReturn(existing);

    Product result = resource.update(1L, updated);

    assertEquals("KALLAX-V2", result.name);
    assertEquals("Updated", result.description);
    assertEquals(BigDecimal.valueOf(59.99), result.price);
    assertEquals(5, result.stock);
  }

  @Test
  void updateShouldThrow422WhenNameIsNull() {
    Product updated = new Product();
    updated.name = null;

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.update(1L, updated));
    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void updateShouldThrow404WhenProductNotFound() {
    Product updated = new Product("KALLAX");
    when(productRepository.findById(99L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.update(99L, updated));
    assertEquals(404, ex.getResponse().getStatus());
  }

  // --- delete ---

  @Test
  void deleteShouldReturn204WhenProductFound() {
    Product existing = new Product("KALLAX");
    existing.id = 1L;
    when(productRepository.findById(1L)).thenReturn(existing);

    var response = resource.delete(1L);

    verify(productRepository).delete(existing);
    assertEquals(204, response.getStatus());
  }

  @Test
  void deleteShouldThrow404WhenNotFound() {
    when(productRepository.findById(99L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.delete(99L));
    assertEquals(404, ex.getResponse().getStatus());
  }
}
