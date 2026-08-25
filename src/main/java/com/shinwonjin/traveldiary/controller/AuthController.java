package com.shinwonjin.traveldiary.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.traveldiary.dto.auth.PasswordResetConfirmRequest;
import com.shinwonjin.traveldiary.dto.auth.PasswordResetRequest;
import com.shinwonjin.traveldiary.dto.auth.PasswordResetVerifyRequest;
import com.shinwonjin.traveldiary.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordResetService passwordResetService;

    public AuthController(
            PasswordResetService passwordResetService
    ) {
        this.passwordResetService =
                passwordResetService;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentMember(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Map.of(
                "memberId", Long.valueOf(jwt.getSubject()),
                "email", jwt.getClaimAsString("email"),
                "nickname", jwt.getClaimAsString("nickname")
        );
    }

    @PostMapping("/password-reset/request")
    public Map<String, String> requestPasswordReset(
            @Valid
            @RequestBody
            PasswordResetRequest request
    ) {
        passwordResetService.requestPasswordReset(
                request.email()
        );

        return Map.of(
                "message",
                "가입된 이메일인 경우 인증번호를 전송했습니다."
        );
    }

    @PostMapping("/password-reset/verify")
    public Map<String, String> verifyPasswordResetCode(
            @Valid
            @RequestBody
            PasswordResetVerifyRequest request
    ) {
        passwordResetService.verifyPasswordResetCode(
                request.email(),
                request.verificationCode()
        );

        return Map.of(
                "message",
                "인증번호가 확인되었습니다."
        );
    }

    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmPasswordReset(
            @Valid
            @RequestBody
            PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmPasswordReset(
                request.email(),
                request.verificationCode(),
                request.newPassword()
        );

        return Map.of(
                "message",
                "비밀번호가 변경되었습니다."
        );
    }
}