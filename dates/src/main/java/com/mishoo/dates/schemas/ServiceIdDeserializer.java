package com.mishoo.dates.schemas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ServiceIdDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return switch (value.trim().toUpperCase()) {
                case "HAIRCUT" -> 1L;
                case "BEARD_TRIM" -> 2L;
                case "HAIRCUT_AND_BEARD" -> 3L;
                case "SHAVE" -> 4L;
                case "HAIR_DYE" -> 5L;
                case "FACIAL_TREATMENT" -> 6L;
                case "EYEBROW_DESIGN" -> 7L;
                case "HAIR_WASH_AND_STYLING" -> 8L;
                default -> 1L;
            };
        }
    }
}
