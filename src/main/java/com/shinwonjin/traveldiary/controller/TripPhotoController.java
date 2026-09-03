package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripPhotoLocationUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoMemoUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoTakenAtUpdateRequest;
import com.shinwonjin.traveldiary.service.TripPhotoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/photos")
@RequiredArgsConstructor
public class TripPhotoController {

    private final TripPhotoService tripPhotoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TripPhotoResponse> uploadTripPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripPhotoService.uploadTripPhoto(
                        memberId,
                        tripId,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TripPhotoResponse>> getTripPhotos(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripPhotoResponse> response =
                tripPhotoService.getTripPhotos(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{photoId}/file")
    public ResponseEntity<Resource> getTripPhotoFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        Resource resource =
                tripPhotoService.getTripPhotoFile(
                        memberId,
                        tripId,
                        photoId
                );

        MediaType mediaType =
                MediaTypeFactory
                        .getMediaType(resource)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deleteTripPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripPhotoService.deleteTripPhoto(
                memberId,
                tripId,
                photoId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{photoId}/memo")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoMemo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @Valid @RequestBody TripPhotoMemoUpdateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripPhotoService.updateTripPhotoMemo(
                        memberId,
                        tripId,
                        photoId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{photoId}/taken-at")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoTakenAt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @RequestBody TripPhotoTakenAtUpdateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripPhotoService.updateTripPhotoTakenAt(
                        memberId,
                        tripId,
                        photoId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{photoId}/location")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @RequestBody TripPhotoLocationUpdateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripPhotoService.updateTripPhotoLocation(
                        memberId,
                        tripId,
                        photoId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{photoId}/location")
    public ResponseEntity<TripPhotoResponse> deleteTripPhotoLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    TripPhotoResponse response =
            tripPhotoService.deleteTripPhotoLocation(
                    memberId,
                    tripId,
                    photoId
            );

    return ResponseEntity.ok(response);
    }
}