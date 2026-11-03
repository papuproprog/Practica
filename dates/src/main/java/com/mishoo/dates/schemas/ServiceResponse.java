package com.mishoo.dates.schemas;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        Double price
) {}
