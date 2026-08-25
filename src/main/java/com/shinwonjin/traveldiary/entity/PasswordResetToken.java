package com.shinwonjin.traveldiary.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(
            name = "code_hash",
            nullable = false,
            length = 255
    )
    private String codeHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            nullable = false
    )
    private boolean verified = false;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    private PasswordResetToken(
            Member member,
            String codeHash,
            LocalDateTime expiresAt
    ) {
        this.member = member;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public static PasswordResetToken create(
            Member member,
            String codeHash,
            LocalDateTime expiresAt
    ) {
        return new PasswordResetToken(
                member,
                codeHash,
                expiresAt
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now()
                .isAfter(expiresAt);
    }

    public void verify() {
        this.verified = true;
    }
}