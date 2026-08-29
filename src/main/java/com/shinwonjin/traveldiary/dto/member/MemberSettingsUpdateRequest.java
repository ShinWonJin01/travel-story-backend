package com.shinwonjin.traveldiary.dto.member;

import jakarta.validation.constraints.NotNull;

public record MemberSettingsUpdateRequest(

        @NotNull
        Boolean invitationNotificationEnabled,

        @NotNull
        Boolean activityNotificationEnabled

) {
}