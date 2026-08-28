package com.shinwonjin.traveldiary.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripResponse;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripCoverImageService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final FileStorageService fileStorageService;

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

        String coverImagePath =
                fileStorageService.storeTripCoverImage(
                        tripId,
                        file
                );

        trip.updateCoverImagePath(coverImagePath);

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

        fileStorageService.deleteTripCoverImage(
                tripId,
                coverImagePath
        );

        trip.updateCoverImagePath(null);

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

        return fileStorageService.loadTripImage(
                tripId,
                coverImagePath
        );
    }
}