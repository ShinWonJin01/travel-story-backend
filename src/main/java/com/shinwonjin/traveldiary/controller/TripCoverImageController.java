package com.shinwonjin.traveldiary.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripResponse;
import com.shinwonjin.traveldiary.service.TripCoverImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/cover-image")
@RequiredArgsConstructor
public class TripCoverImageController {

    private final TripCoverImageService tripCoverImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TripResponse> uploadCoverImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripCoverImageService.uploadCoverImage(
                        memberId,
                        tripId,
                        file
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> getTripCoverImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        Resource resource =
                tripCoverImageService.getTripCoverImage(
                        memberId,
                        tripId
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

    @DeleteMapping
    public ResponseEntity<TripResponse> deleteCoverImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripCoverImageService.deleteCoverImage(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }
}