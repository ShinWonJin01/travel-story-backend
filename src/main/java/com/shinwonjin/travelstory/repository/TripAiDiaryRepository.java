package com.shinwonjin.travelstory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shinwonjin.travelstory.entity.TripAiDiary;

import java.util.Optional;

public interface TripAiDiaryRepository extends JpaRepository<TripAiDiary, Long> {

    Optional<TripAiDiary> findByTripId(Long tripId);

    void deleteByTripId(Long tripId);
}