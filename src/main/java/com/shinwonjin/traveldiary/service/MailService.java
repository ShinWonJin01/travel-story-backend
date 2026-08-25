package com.shinwonjin.traveldiary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public MailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}")
            String mailUsername
    ) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
    }

    public void sendPasswordResetCode(
            String email,
            String verificationCode
    ) {
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject(
                "[Travel Diary] 비밀번호 재설정 인증번호"
        );
        message.setText(
                """
                Travel Diary 비밀번호 재설정 인증번호입니다.

                인증번호: %s

                인증번호는 일정 시간 동안만 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(verificationCode)
        );

        mailSender.send(message);
    }
}