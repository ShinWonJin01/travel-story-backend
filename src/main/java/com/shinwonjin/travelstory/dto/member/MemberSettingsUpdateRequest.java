package com.shinwonjin.travelstory.dto.member;

import jakarta.validation.constraints.NotNull;

public record MemberSettingsUpdateRequest(

        @NotNull
        Boolean invitationNotificationEnabled,

        @NotNull
        Boolean activityNotificationEnabled

) {
}