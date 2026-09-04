package com.shinwonjin.travelstory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.travelstory.dto.trip.TripAiDiaryResponse;
import com.shinwonjin.travelstory.service.TripAiDiaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/ai-diary")
@RequiredArgsConstructor
public class TripAiDiaryController {

    private final TripAiDiaryService tripAiDiaryService;

    @GetMapping
    public ResponseEntity<TripAiDiaryResponse> getTripAiDiary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        return tripAiDiaryService
                .getTripAiDiary(memberId, tripId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.noContent().build()
                );
    }

    @PostMapping
    public ResponseEntity<TripAiDiaryResponse> generateTripAiDiary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripAiDiaryResponse response =
                tripAiDiaryService.generateTripAiDiary(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }
}