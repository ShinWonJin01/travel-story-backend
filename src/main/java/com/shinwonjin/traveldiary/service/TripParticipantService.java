package com.shinwonjin.traveldiary.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.trip.TripParticipantResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.NotificationType;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberRole;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.repository.MemberRepository;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripParticipantService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final TripMemberRepository tripMemberRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<TripParticipantResponse> getTripParticipants(
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
                    "이 여행의 참여자 정보를 조회할 권한이 없습니다."
            );
        }

        return tripMemberRepository
                .findAllByTripIdAndStatusOrderByCreatedAtAsc(
                        tripId,
                        TripMemberStatus.ACCEPTED
                )
                .stream()
                .map(TripParticipantResponse::from)
                .toList();
    }

    @Transactional
    public void leaveTrip(
            Long memberId,
            Long tripId
    ) {
        TripMember tripMember = tripMemberRepository
                .findByTripIdAndMemberId(
                        tripId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "참여 중인 여행을 찾을 수 없습니다."
                        )
                );

        if (tripMember.getRole() == TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "여행 생성자는 여행에서 나갈 수 없습니다."
            );
        }

        if (tripMember.getStatus() != TripMemberStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                    "참여 중인 여행만 나갈 수 있습니다."
            );
        }

        Trip trip = tripMember.getTrip();

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        notificationService.createNotification(
                trip.getOwner(),
                member,
                trip,
                NotificationType.MEMBER_LEFT_TRIP,
                member.getNickname()
                        + "님이 '"
                        + trip.getTitle()
                        + "'에서 나갔습니다."
        );

        tripMemberRepository.delete(tripMember);
    }
}