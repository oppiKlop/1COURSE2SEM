package com.todolist.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@Converter
public class TagsJsonConverter implements AttributeConverter<Set<String>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Set<String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return "[]";
    }

    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize tags to JSON", e);
    }
  }

  @Override
  public Set<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptySet();
    }

    try {
      return Set.copyOf(objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(Set.class, String.class)));
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to deserialize tags JSON", e);
    }
  }
}

