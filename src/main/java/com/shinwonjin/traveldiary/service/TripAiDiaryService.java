package com.shinwonjin.traveldiary.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.trip.TripAiDiaryResponse;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripAiDiary;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.entity.TripPhoto;
import com.shinwonjin.traveldiary.repository.TripAiDiaryRepository;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripPhotoRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripAiDiaryService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPhotoRepository tripPhotoRepository;
    private final TripAiDiaryRepository tripAiDiaryRepository;
    private final GeminiService geminiService;

    @Transactional(readOnly = true)
    public Optional<TripAiDiaryResponse> getTripAiDiary(
            Long memberId,
            Long tripId
    ) {
        validateTripAccess(memberId, tripId);

        return tripAiDiaryRepository
                .findByTripId(tripId)
                .map(TripAiDiaryResponse::from);
    }

    @Transactional
    public TripAiDiaryResponse generateTripAiDiary(
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

        if (!trip.getOwner().getId().equals(memberId)) {
            throw new IllegalArgumentException(
                    "여행 소유자만 AI 여행기를 생성할 수 있습니다."
            );
        }

        List<TripPhoto> photos =
                tripPhotoRepository
                        .findAllByTripIdOrderByCreatedAtAsc(
                                tripId
                        );

        String prompt =
                buildAiDiaryPrompt(trip, photos);

        String content =
                geminiService.generate(prompt);

        TripAiDiary diary =
                tripAiDiaryRepository
                        .findByTripId(tripId)
                        .orElse(null);

        if (diary == null) {
            diary = TripAiDiary.create(
                    trip,
                    content
            );

            tripAiDiaryRepository.save(diary);
        } else {
            diary.updateContent(content);
        }

        return TripAiDiaryResponse.from(diary);
    }

    private void validateTripAccess(
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
    }

    private String buildAiDiaryPrompt(
            Trip trip,
            List<TripPhoto> photos
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                다음 여행 정보를 바탕으로 자연스러운 여행기를 작성해 주세요.

                작성 규칙:
                - 실제 제공된 정보만 사용해 주세요.
                - 제공되지 않은 장소, 행동, 감정은 임의로 만들어내지 마세요.
                - 사진의 촬영시간 순서에 따라 여행의 흐름을 구성해 주세요.
                - 사진 메모가 있다면 내용을 자연스럽게 반영해 주세요.
                - 단순한 정보 나열이 아니라 하나의 자연스러운 여행 기록으로 작성해 주세요.
                - 제목은 따로 만들지 말고 본문만 작성해 주세요.
                - 한국어로 작성해 주세요.

                [여행 정보]
                """);

        prompt.append("여행 제목: ")
                .append(trip.getTitle())
                .append("\n");

        prompt.append("대표 지역: ")
                .append(trip.getDestination())
                .append("\n");

        prompt.append("시작일: ")
                .append(trip.getStartDate())
                .append("\n");

        prompt.append("종료일: ")
                .append(
                        trip.getEndDate() == null
                                ? "미정"
                                : trip.getEndDate()
                )
                .append("\n");

        if (
                trip.getDescription() != null
                && !trip.getDescription().isBlank()
        ) {
            prompt.append("여행 설명: ")
                    .append(
                            trip.getDescription().trim()
                    )
                    .append("\n");
        }

        prompt.append("\n[사진 기록]\n");

        List<TripPhoto> sortedPhotos =
                photos.stream()
                        .sorted(
                                Comparator.comparing(
                                        TripPhoto::getTakenAt,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        if (sortedPhotos.isEmpty()) {
            prompt.append(
                    "등록된 사진 기록이 없습니다.\n"
            );
        } else {
            for (
                    int i = 0;
                    i < sortedPhotos.size();
                    i++
            ) {
                TripPhoto photo =
                        sortedPhotos.get(i);

                prompt.append("\n사진 ")
                        .append(i + 1)
                        .append("\n");

                prompt.append("- 촬영시간: ")
                        .append(
                                photo.getTakenAt() == null
                                        ? "정보 없음"
                                        : photo.getTakenAt()
                        )
                        .append("\n");

                prompt.append("- 위치: ")
                        .append(
                                photo.getLocationName() == null
                                        || photo.getLocationName().isBlank()
                                        ? "정보 없음"
                                        : photo.getLocationName()
                        )
                        .append("\n");

                if (
                        photo.getMemo() != null
                        && !photo.getMemo().isBlank()
                ) {
                    prompt.append("- 메모: ")
                            .append(
                                    photo.getMemo().trim()
                            )
                            .append("\n");
                }
            }
        }

        return prompt.toString();
    }
}