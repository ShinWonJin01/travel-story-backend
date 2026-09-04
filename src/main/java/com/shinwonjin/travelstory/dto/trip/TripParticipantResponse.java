package com.shinwonjin.travelstory.dto.trip;

import com.shinwonjin.travelstory.entity.Member;
import com.shinwonjin.travelstory.entity.TripMember;
import com.shinwonjin.travelstory.entity.TripMemberRole;

public record TripParticipantResponse(
        Long memberId,
        String nickname,
        String profileImagePath,
        TripMemberRole role
) {

    public static TripParticipantResponse from(
            TripMember tripMember
    ) {
        Member member = tripMember.getMember();

        return new TripParticipantResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImagePath(),
                tripMember.getRole()
        );
    }
}