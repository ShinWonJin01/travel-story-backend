package com.shinwonjin.travelstory.dto.home;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        Long tripId,
        String tripTitle,
        Long actorId,
        String actorNickname,
        String actorProfileImagePath,
        int photoCount,
        LocalDateTime createdAt
) {
}