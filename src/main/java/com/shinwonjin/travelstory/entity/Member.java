package com.shinwonjin.travelstory.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(
            name = "profile_image_path",
            length = 500
    )
    private String profileImagePath;

    @Column(
            name = "profile_image_cloudinary_public_id",
            length = 500
    )
    private String profileImageCloudinaryPublicId;

    @Column(
            name = "invitation_notification_enabled",
            nullable = false
    )
    private boolean invitationNotificationEnabled = true;

    @Column(
            name = "activity_notification_enabled",
            nullable = false
    )
    private boolean activityNotificationEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Member(
            String name,
            String email,
            String password,
            String nickname
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public static Member create(
            String name,
            String email,
            String encodedPassword,
            String nickname
    ) {
        return new Member(
                name,
                email,
                encodedPassword,
                nickname
        );
    }

    public void changePassword(
            String encodedPassword
    ) {
        this.password = encodedPassword;
    }

    public void updateProfile(
            String name,
            String nickname
    ) {
        this.name = name;
        this.nickname = nickname;
    }

    public void updateProfileImage(
            String profileImagePath,
            String profileImageCloudinaryPublicId
    ) {
        this.profileImagePath =
                profileImagePath;

        this.profileImageCloudinaryPublicId =
                profileImageCloudinaryPublicId;
    }

    public void clearProfileImage() {
        this.profileImagePath = null;
        this.profileImageCloudinaryPublicId = null;
    }

    public void updateSettings(
            boolean invitationNotificationEnabled,
            boolean activityNotificationEnabled
    ) {
        this.invitationNotificationEnabled =
                invitationNotificationEnabled;
        this.activityNotificationEnabled =
                activityNotificationEnabled;
    }
}