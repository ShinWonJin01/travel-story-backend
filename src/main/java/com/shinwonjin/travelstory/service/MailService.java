package com.shinwonjin.travelstory.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final GmailApiService gmailApiService;

    public void sendPasswordResetCode(
            String email,
            String verificationCode
    ) {
        String subject =
                "[Travel Story] 비밀번호 재설정 인증번호";

        String body =
                """
                Travel Story 비밀번호 재설정 인증번호입니다.

                인증번호: %s

                인증번호는 일정 시간 동안만 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(verificationCode);

        gmailApiService.sendTextEmail(
                email,
                subject,
                body
        );
    }
}