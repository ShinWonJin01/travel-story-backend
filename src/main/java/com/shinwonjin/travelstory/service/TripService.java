package com.shinwonjin.travelstory.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.shinwonjin.travelstory.dto.trip.TripCreateRequest;
import com.shinwonjin.travelstory.dto.trip.TripListResponse;
import com.shinwonjin.travelstory.dto.trip.TripResponse;
import com.shinwonjin.travelstory.dto.trip.TripSummaryResponse;
import com.shinwonjin.travelstory.dto.trip.TripUpdateRequest;
import com.shinwonjin.travelstory.entity.Member;
import com.shinwonjin.travelstory.entity.NotificationType;
import com.shinwonjin.travelstory.entity.Trip;
import com.shinwonjin.travelstory.entity.TripMember;
import com.shinwonjin.travelstory.entity.TripMemberRole;
import com.shinwonjin.travelstory.entity.TripMemberStatus;
import com.shinwonjin.travelstory.repository.MemberRepository;
import com.shinwonjin.travelstory.repository.TripAiDiaryRepository;
import com.shinwonjin.travelstory.repository.TripMemberRepository;
import com.shinwonjin.travelstory.repository.TripPhotoRepository;
import com.shinwonjin.travelstory.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPhotoRepository tripPhotoRepository;
    private final FileStorageService fileStorageService;
    private final CloudinaryService cloudinaryService;
    private final TripAiDiaryRepository tripAiDiaryRepository;
    private final NotificationService notificationService;

    @Transactional
    public TripResponse createTrip(
            Long memberId,
            TripCreateRequest request
    ) {
        validateTripDates(
                request.startDate(),
                request.endDate()
        );

        Member owner = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        Trip trip = Trip.create(
                owner,
                request.title().strip(),
                request.destination().strip(),
                request.startDate(),
                request.endDate(),
                request.description() == null
                        ? ""
                        : request.description().strip()
        );

        Trip savedTrip = tripRepository.save(trip);

        TripMember ownerTripMember =
                TripMember.createOwner(
                        savedTrip,
                        owner
                );

        tripMemberRepository.save(ownerTripMember);

        return TripResponse.from(savedTrip);
    }

    @Transactional
    public TripResponse updateTrip(
            Long memberId,
            Long tripId,
            TripUpdateRequest request
    ) {
        validateTripDates(
                request.startDate(),
                request.endDate()
        );

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

        trip.update(
                request.title().strip(),
                request.destination().strip(),
                request.startDate(),
                request.endDate(),
                request.description() == null
                        ? ""
                        : request.description().strip()
        );

        List<TripMember> participants =
                tripMemberRepository
                        .findAllByTripIdAndStatusOrderByCreatedAtAsc(
                                tripId,
                                TripMemberStatus.ACCEPTED
                        );

        for (TripMember participant : participants) {
            if (participant.getRole() != TripMemberRole.MEMBER) {
                continue;
            }

            notificationService.createNotification(
                    participant.getMember(),
                    trip.getOwner(),
                    trip,
                    NotificationType.TRIP_UPDATED,
                    trip.getOwner().getNickname()
                            + "님이 '"
                            + trip.getTitle()
                            + "'의 여행 정보를 수정했습니다."
            );
        }

        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    public List<TripListResponse> getMyTrips(
            Long memberId
    ) {
        List<Trip> ownedTrips =
                tripRepository
                        .findAllByOwnerIdOrderByCreatedAtDesc(
                                memberId
                        );

        List<TripMember> participatingTripMembers =
                tripMemberRepository
                        .findAllByMemberIdAndRoleAndStatusOrderByCreatedAtDesc(
                                memberId,
                                TripMemberRole.MEMBER,
                                TripMemberStatus.ACCEPTED
                        );

        return Stream.concat(
                        ownedTrips
                                .stream()
                                .map(trip ->
                                        TripListResponse.from(
                                                trip,
                                                TripMemberRole.OWNER,
                                                countAcceptedParticipants(
                                                        trip.getId()
                                                )
                                        )
                                ),

                        participatingTripMembers
                                .stream()
                                .map(tripMember ->
                                        TripListResponse.from(
                                                tripMember.getTrip(),
                                                TripMemberRole.MEMBER,
                                                countAcceptedParticipants(
                                                        tripMember
                                                                .getTrip()
                                                                .getId()
                                                )
                                        )
                                )
                )
                .sorted(
                        Comparator.comparing(
                                TripListResponse::createdAt
                        ).reversed()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public TripSummaryResponse getTripSummary(
            Long memberId
    ) {
        long ownedCount =
                tripRepository.countByOwnerId(memberId);

        long participatingCount =
                tripMemberRepository
                        .countByMemberIdAndRoleAndStatus(
                                memberId,
                                TripMemberRole.MEMBER,
                                TripMemberStatus.ACCEPTED
                        );

        long totalCount =
                ownedCount + participatingCount;

        return new TripSummaryResponse(
                totalCount,
                ownedCount,
                participatingCount
        );
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(
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
                    "이 여행을 조회할 권한이 없습니다."
            );
        }

        return TripResponse.from(trip);
    }

    @Transactional
    public void deleteTrip(
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

        List<String> cloudinaryPublicIds =
                collectCloudinaryPublicIds(
                        trip
                );

        List<TripMember> participants =
                tripMemberRepository
                        .findAllByTripIdAndStatusOrderByCreatedAtAsc(
                                tripId,
                                TripMemberStatus.ACCEPTED
                        );

        for (TripMember participant : participants) {
            if (participant.getRole() != TripMemberRole.MEMBER) {
                continue;
            }

            notificationService.createNotification(
                    participant.getMember(),
                    trip.getOwner(),
                    null,
                    NotificationType.TRIP_DELETED,
                    trip.getOwner().getNickname()
                            + "님이 '"
                            + trip.getTitle()
                            + "'을 삭제했습니다."
            );
        }

        tripAiDiaryRepository.deleteByTripId(tripId);
        tripPhotoRepository.deleteAllByTripId(tripId);
        tripMemberRepository.deleteAllByTripId(tripId);

        notificationService.clearTrip(tripId);

        tripRepository.delete(trip);

        registerTripImageCleanup(
                tripId,
                cloudinaryPublicIds
        );
    }

    @Transactional
    public void deleteTripForMemberWithdrawal(
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

        List<String> cloudinaryPublicIds =
                collectCloudinaryPublicIds(
                        trip
                );

        List<TripMember> participants =
                tripMemberRepository
                        .findAllByTripIdAndStatusOrderByCreatedAtAsc(
                                tripId,
                                TripMemberStatus.ACCEPTED
                        );

        String ownerNickname =
                trip.getOwner().getNickname();

        String tripTitle =
                trip.getTitle();

        for (TripMember participant : participants) {
            if (participant.getRole() != TripMemberRole.MEMBER) {
                continue;
            }

            notificationService.createNotification(
                    participant.getMember(),
                    null,
                    null,
                    NotificationType.TRIP_DELETED,
                    ownerNickname
                            + "님이 회원 탈퇴하여 '"
                            + tripTitle
                            + "'이 삭제되었습니다."
            );
        }

        tripAiDiaryRepository.deleteByTripId(tripId);
        tripPhotoRepository.deleteAllByTripId(tripId);
        tripMemberRepository.deleteAllByTripId(tripId);

        notificationService.clearTrip(tripId);

        tripRepository.delete(trip);

        registerTripImageCleanup(
                tripId,
                cloudinaryPublicIds
        );
    }

    private List<String> collectCloudinaryPublicIds(
            Trip trip
    ) {
        Long tripId = trip.getId();

        Stream<String> coverImagePublicId =
                Stream.of(
                        trip.getCoverImageCloudinaryPublicId()
                );

        Stream<String> photoPublicIds =
                tripPhotoRepository
                        .findAllByTripIdOrderByCreatedAtAsc(
                                tripId
                        )
                        .stream()
                        .map(photo ->
                                photo.getCloudinaryPublicId()
                        );

        return Stream.concat(
                        coverImagePublicId,
                        photoPublicIds
                )
                .filter(publicId ->
                        publicId != null
                        && !publicId.isBlank()
                )
                .distinct()
                .toList();
    }

    private void registerTripImageCleanup(
            Long tripId,
            List<String> cloudinaryPublicIds
    ) {
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {
                                for (
                                        String publicId
                                        : cloudinaryPublicIds
                                ) {
                                    cloudinaryService.deleteImage(
                                            publicId
                                    );
                                }

                                fileStorageService.deleteTripFiles(
                                        tripId
                                );
                            }
                        }
                );
    }

    private void validateTripDates(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        if (
                endDate != null
                && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }

    private long countAcceptedParticipants(
            Long tripId
    ) {
        return tripMemberRepository
                .countByTripIdAndStatus(
                        tripId,
                        TripMemberStatus.ACCEPTED
                );
    }
}