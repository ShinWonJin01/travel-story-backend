package com.shinwonjin.travelstory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shinwonjin.travelstory.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Trip> findByIdAndOwnerId(
            Long tripId,
            Long ownerId
    );

    long countByOwnerId(Long ownerId);
}