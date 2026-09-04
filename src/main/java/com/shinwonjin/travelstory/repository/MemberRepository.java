package com.shinwonjin.travelstory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shinwonjin.travelstory.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}