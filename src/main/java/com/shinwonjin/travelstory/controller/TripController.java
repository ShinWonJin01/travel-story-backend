package com.shinwonjin.travelstory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.travelstory.dto.trip.TripCreateRequest;
import com.shinwonjin.travelstory.dto.trip.TripListResponse;
import com.shinwonjin.travelstory.dto.trip.TripResponse;
import com.shinwonjin.travelstory.dto.trip.TripSummaryResponse;
import com.shinwonjin.travelstory.dto.trip.TripUpdateRequest;
import com.shinwonjin.travelstory.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TripCreateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.createTrip(
                        memberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TripListResponse>> getMyTrips(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripListResponse> response =
                tripService.getMyTrips(memberId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TripSummaryResponse> getTripSummary(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripSummaryResponse response =
                tripService.getTripSummary(memberId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.getTrip(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @Valid @RequestBody TripUpdateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.updateTrip(
                        memberId,
                        tripId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripService.deleteTrip(
                memberId,
                tripId
        );

        return ResponseEntity.noContent().build();
    }
}