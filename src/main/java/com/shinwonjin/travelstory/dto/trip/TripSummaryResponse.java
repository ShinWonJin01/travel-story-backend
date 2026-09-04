package com.shinwonjin.travelstory.dto.trip;

public record TripSummaryResponse(
        long totalCount,
        long ownedCount,
        long participatingCount
) {
}