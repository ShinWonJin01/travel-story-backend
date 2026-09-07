package com.shinwonjin.travelstory.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.travelstory.dto.trip.TripResponse;
import com.shinwonjin.travelstory.entity.Trip;
import com.shinwonjin.travelstory.entity.TripMemberStatus;
import com.shinwonjin.travelstory.repository.TripMemberRepository;
import com.shinwonjin.travelstory.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripCoverImageService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final FileStorageService fileStorageService;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public TripResponse uploadCoverImage(
            Long memberId,
            Long tripId,
            MultipartFile file
    ) {
        Trip trip = tripRepository
                .findByIdAndOwnerId(
                        tripId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        fileStorageService.validateTripCoverImage(file);

        CloudinaryService.UploadResult uploadResult =
                cloudinaryService.uploadImage(
                        file,
                        "travel-story/trips/" + tripId + "/cover"
                );

        String previousCoverImagePath =
                trip.getCoverImagePath();

        String previousCloudinaryPublicId =
                trip.getCoverImageCloudinaryPublicId();

        if (
                previousCoverImagePath != null
                && !previousCoverImagePath.isBlank()
        ) {
        if (
                previousCloudinaryPublicId != null
                && !previousCloudinaryPublicId.isBlank()
        ) {
                cloudinaryService.deleteImage(
                        previousCloudinaryPublicId
                );
        } else {
                fileStorageService.deleteTripCoverImage(
                        tripId,
                        previousCoverImagePath
                );
        }
        }

        trip.updateCoverImage(
                uploadResult.url(),
                uploadResult.publicId()
        );

        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse deleteCoverImage(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findByIdAndOwnerId(
                        tripId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        String coverImagePath =
                trip.getCoverImagePath();

        if (
                coverImagePath == null
                || coverImagePath.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "삭제할 대표 이미지가 없습니다."
            );
        }

        String cloudinaryPublicId =
                trip.getCoverImageCloudinaryPublicId();

        if (
                cloudinaryPublicId != null
                && !cloudinaryPublicId.isBlank()
        ) {
        cloudinaryService.deleteImage(
                cloudinaryPublicId
        );
        } else {
        fileStorageService.deleteTripCoverImage(
                tripId,
                coverImagePath
        );
        }

        trip.clearCoverImage();

        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    public Resource getTripCoverImage(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        boolean isOwner =
                trip.getOwner()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (!isOwner && !isAcceptedMember) {
            throw new IllegalArgumentException(
                    "이 여행의 대표 이미지를 조회할 권한이 없습니다."
            );
        }

        String coverImagePath =
                trip.getCoverImagePath();

        if (
                coverImagePath == null
                || coverImagePath.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "등록된 대표 이미지가 없습니다."
            );
        }

        String cloudinaryPublicId =
                trip.getCoverImageCloudinaryPublicId();

        if (
                cloudinaryPublicId != null
                && !cloudinaryPublicId.isBlank()
        ) {
        return cloudinaryService.loadImage(
                coverImagePath
        );
        }

        return fileStorageService.loadTripImage(
                tripId,
                coverImagePath
        );
    }
}