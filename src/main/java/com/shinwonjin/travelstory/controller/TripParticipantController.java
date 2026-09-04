package com.shinwonjin.travelstory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.travelstory.dto.trip.TripParticipantResponse;
import com.shinwonjin.travelstory.service.TripParticipantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/members")
@RequiredArgsConstructor
public class TripParticipantController {

    private final TripParticipantService tripParticipantService;

    @GetMapping
    public ResponseEntity<List<TripParticipantResponse>> getTripParticipants(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripParticipantResponse> response =
                tripParticipantService.getTripParticipants(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripParticipantService.leaveTrip(
                memberId,
                tripId
        );

        return ResponseEntity.noContent().build();
    }
}