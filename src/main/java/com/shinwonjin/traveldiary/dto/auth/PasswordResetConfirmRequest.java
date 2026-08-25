package com.shinwonjin.traveldiary.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(

        @NotBlank(
                message = "이메일을 입력해 주세요."
        )
        @Email(
                message = "올바른 이메일 형식을 입력해 주세요."
        )
        String email,

        @NotBlank(
                message = "인증번호를 입력해 주세요."
        )
        @Pattern(
                regexp = "\\d{6}",
                message = "인증번호는 6자리 숫자입니다."
        )
        String verificationCode,

        @NotBlank(
                message = "새 비밀번호를 입력해 주세요."
        )
        @Size(
                min = 8,
                max = 30,
                message = "비밀번호는 8자 이상 30자 이하로 입력해 주세요."
        )
        String newPassword

) {
}