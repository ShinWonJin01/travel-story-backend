package com.shinwonjin.travelstory.dto.trip;

public record TripPhotoLocationUpdateRequest(
        Double latitude,
        Double longitude,
        String locationName
) {
}