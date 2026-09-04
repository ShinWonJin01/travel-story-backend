package com.shinwonjin.travelstory.dto.member;

import com.shinwonjin.travelstory.entity.Member;

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