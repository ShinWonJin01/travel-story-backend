package com.shinwonjin.travelstory.dto.trip;

import java.time.LocalDateTime;

import com.shinwonjin.travelstory.entity.TripAiDiary;

public record TripAiDiaryResponse(
        Long id,
        Long tripId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TripAiDiaryResponse from(TripAiDiary diary) {
        return new TripAiDiaryResponse(
                diary.getId(),
                diary.getTrip().getId(),
                diary.getContent(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}