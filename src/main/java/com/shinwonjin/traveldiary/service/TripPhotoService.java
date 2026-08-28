package com.shinwonjin.traveldiary.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripPhotoLocationUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoMemoUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoTakenAtUpdateRequest;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.entity.TripPhoto;
import com.shinwonjin.traveldiary.repository.MemberRepository;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripPhotoRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPhotoService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPhotoRepository tripPhotoRepository;
    private final FileStorageService fileStorageService;
    private final PhotoMetadataService photoMetadataService;
    private final ReverseGeocodingService reverseGeocodingService;

    @Transactional
    public TripPhotoResponse uploadTripPhoto(
            Long memberId,
            Long tripId,
            MultipartFile file
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
                    "이 여행에 사진을 등록할 권한이 없습니다."
            );
        }

        Member uploadedBy = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        PhotoMetadataService.PhotoMetadata metadata =
                photoMetadataService.extractMetadata(file);

        LocalDateTime takenAt = metadata.takenAt();
        Double latitude = metadata.latitude();
        Double longitude = metadata.longitude();

        String locationName = null;

        if (latitude != null && longitude != null) {
            locationName =
                    reverseGeocodingService.getLocationName(
                            latitude,
                            longitude
                    );
        }

        String filePath =
                fileStorageService.storeTripPhoto(
                        tripId,
                        file
                );

        String originalFileName =
                file.getOriginalFilename();

        if (
                originalFileName == null
                || originalFileName.isBlank()
        ) {
            originalFileName = "photo";
        }

        TripPhoto tripPhoto = TripPhoto.create(
                trip,
                uploadedBy,
                filePath,
                originalFileName,
                takenAt,
                latitude,
                longitude,
                locationName,
                null
        );

        TripPhoto savedPhoto =
                tripPhotoRepository.save(tripPhoto);

        return TripPhotoResponse.from(savedPhoto);
    }

    @Transactional(readOnly = true)
    public List<TripPhotoResponse> getTripPhotos(
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
                    "이 여행의 사진을 조회할 권한이 없습니다."
            );
        }

        return tripPhotoRepository
                .findAllByTripIdOrderByCreatedAtAsc(tripId)
                .stream()
                .map(TripPhotoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource getTripPhotoFile(
            Long memberId,
            Long tripId,
            Long photoId
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        Trip trip = tripPhoto.getTrip();

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
                    "이 여행의 사진을 조회할 권한이 없습니다."
            );
        }

        return fileStorageService.loadTripImage(
                tripId,
                tripPhoto.getFilePath()
        );
    }

    @Transactional
    public void deleteTripPhoto(
            Long memberId,
            Long tripId,
            Long photoId
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (
                !isOwner
                && !(isUploader && isAcceptedMember)
        ) {
            throw new IllegalArgumentException(
                    "이 사진을 삭제할 권한이 없습니다."
            );
        }

        fileStorageService.deleteTripPhoto(
                tripId,
                tripPhoto.getFilePath()
        );

        tripPhotoRepository.delete(tripPhoto);
    }

    @Transactional
    public TripPhotoResponse updateTripPhotoMemo(
            Long memberId,
            Long tripId,
            Long photoId,
            TripPhotoMemoUpdateRequest request
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (
                !isOwner
                && !(isUploader && isAcceptedMember)
        ) {
            throw new IllegalArgumentException(
                    "이 사진의 메모를 수정할 권한이 없습니다."
            );
        }

        tripPhoto.updateMemo(request.memo());

        return TripPhotoResponse.from(tripPhoto);
    }

    @Transactional
    public TripPhotoResponse updateTripPhotoTakenAt(
            Long memberId,
            Long tripId,
            Long photoId,
            TripPhotoTakenAtUpdateRequest request
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (
                !isOwner
                && !(isUploader && isAcceptedMember)
        ) {
            throw new IllegalArgumentException(
                    "이 사진의 촬영시간을 수정할 권한이 없습니다."
            );
        }

        tripPhoto.updateTakenAt(request.takenAt());

        return TripPhotoResponse.from(tripPhoto);
    }

    @Transactional
    public TripPhotoResponse updateTripPhotoLocation(
            Long memberId,
            Long tripId,
            Long photoId,
            TripPhotoLocationUpdateRequest request
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (
                !isOwner
                && !(isUploader && isAcceptedMember)
        ) {
            throw new IllegalArgumentException(
                    "이 사진의 위치를 수정할 권한이 없습니다."
            );
        }

        Double latitude = request.latitude();
        Double longitude = request.longitude();

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "위도와 경도를 모두 입력해 주세요."
            );
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "올바르지 않은 위도입니다."
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "올바르지 않은 경도입니다."
            );
        }

        String locationName;

        if (
                request.locationName() != null
                && !request.locationName().isBlank()
        ) {
            locationName =
                    request.locationName().trim();
        } else {
            locationName =
                    reverseGeocodingService.getLocationName(
                            latitude,
                            longitude
                    );
        }

        tripPhoto.updateLocation(
                latitude,
                longitude,
                locationName
        );

        return TripPhotoResponse.from(tripPhoto);
    }
}