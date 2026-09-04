package com.shinwonjin.travelstory.dto.trip;

import java.time.LocalDateTime;

import com.shinwonjin.travelstory.entity.Member;
import com.shinwonjin.travelstory.entity.TripPhoto;

public record TripPhotoResponse(
        Long id,
        Long tripId,
        Long uploadedByMemberId,
        String uploadedByNickname,
        String filePath,
        String originalFileName,
        LocalDateTime takenAt,
        Double latitude,
        Double longitude,
        String locationName,
        String memo,
        LocalDateTime createdAt
) {
    public static TripPhotoResponse from(TripPhoto tripPhoto) {
        Member uploadedBy = tripPhoto.getUploadedBy();

        return new TripPhotoResponse(
                tripPhoto.getId(),
                tripPhoto.getTrip().getId(),
                uploadedBy == null ? null : uploadedBy.getId(),
                uploadedBy == null ? null : uploadedBy.getNickname(),
                tripPhoto.getFilePath(),
                tripPhoto.getOriginalFileName(),
                tripPhoto.getTakenAt(),
                tripPhoto.getLatitude(),
                tripPhoto.getLongitude(),
                tripPhoto.getLocationName(),
                tripPhoto.getMemo(),
                tripPhoto.getCreatedAt()
        );
    }
}