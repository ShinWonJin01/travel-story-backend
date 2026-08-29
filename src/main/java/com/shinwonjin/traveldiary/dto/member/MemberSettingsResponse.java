package com.shinwonjin.traveldiary.dto.member;

import com.shinwonjin.traveldiary.entity.Member;

public record MemberSettingsResponse(

        boolean invitationNotificationEnabled,
        boolean activityNotificationEnabled

) {

    public static MemberSettingsResponse from(
            Member member
    ) {
        return new MemberSettingsResponse(
                member.isInvitationNotificationEnabled(),
                member.isActivityNotificationEnabled()
        );
    }
}