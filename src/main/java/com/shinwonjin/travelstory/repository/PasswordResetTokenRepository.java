package com.shinwonjin.travelstory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shinwonjin.travelstory.entity.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken>
            findTopByMemberIdOrderByCreatedAtDesc(
                    Long memberId
            );

    void deleteAllByMemberId(Long memberId);
}