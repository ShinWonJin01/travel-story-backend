package com.shinwonjin.travelstory.dto.location;

public record LocationSearchResponse(
        String id,
        String name,
        String address,
        double latitude,
        double longitude
) {
}