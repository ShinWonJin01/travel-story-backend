package com.shinwonjin.travelstory.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.travelstory.entity.Member;
import com.shinwonjin.travelstory.entity.PasswordResetToken;
import com.shinwonjin.travelstory.repository.MemberRepository;
import com.shinwonjin.travelstory.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    private static final int CODE_BOUND = 900000;
    private static final int CODE_OFFSET = 100000;
    private static final int EXPIRATION_MINUTES = 5;

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository
            passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public PasswordResetService(
            MemberRepository memberRepository,
            PasswordResetTokenRepository
                    passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService
    ) {
        this.memberRepository = memberRepository;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public void requestPasswordReset(
            String email
    ) {
        String normalizedEmail =
                email.trim().toLowerCase();

        Member member = memberRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        /*
         * 가입되지 않은 이메일이어도
         * 외부에 회원가입 여부가 노출되지 않도록
         * 오류를 발생시키지 않고 종료합니다.
         */
        if (member == null) {
            return;
        }

        passwordResetTokenRepository
                .deleteAllByMemberId(
                        member.getId()
                );

        String verificationCode =
                generateVerificationCode();

        String codeHash =
                passwordEncoder.encode(
                        verificationCode
                );

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusMinutes(
                                EXPIRATION_MINUTES
                        );

        PasswordResetToken token =
                PasswordResetToken.create(
                        member,
                        codeHash,
                        expiresAt
                );

        passwordResetTokenRepository.save(
                token
        );

        mailService.sendPasswordResetCode(
                member.getEmail(),
                verificationCode
        );
    }

    @Transactional
    public void verifyPasswordResetCode(
            String email,
            String verificationCode
    ) {
        String normalizedEmail =
                email.trim().toLowerCase();

        Member member = memberRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "인증번호가 올바르지 않거나 만료되었습니다."
                        )
                );

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findTopByMemberIdOrderByCreatedAtDesc(
                                member.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "인증번호가 올바르지 않거나 만료되었습니다."
                                )
                        );

        if (token.isExpired()) {
            passwordResetTokenRepository
                    .deleteAllByMemberId(
                            member.getId()
                    );

            throw new IllegalArgumentException(
                    "인증번호가 올바르지 않거나 만료되었습니다."
            );
        }

        if (!passwordEncoder.matches(
                verificationCode,
                token.getCodeHash()
        )) {
            throw new IllegalArgumentException(
                    "인증번호가 올바르지 않거나 만료되었습니다."
            );
        }

        token.verify();
    }

    @Transactional
    public void confirmPasswordReset(
            String email,
            String verificationCode,
            String newPassword
    ) {
        String normalizedEmail =
                email.trim().toLowerCase();

        if (
                newPassword == null
                        || newPassword.length() < 8
                        || newPassword.length() > 30
        ) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상 30자 이하로 입력해 주세요."
            );
        }

        Member member = memberRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "비밀번호 재설정 인증이 필요합니다."
                        )
                );

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findTopByMemberIdOrderByCreatedAtDesc(
                                member.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "비밀번호 재설정 인증이 필요합니다."
                                )
                        );

        if (token.isExpired()) {
            passwordResetTokenRepository
                    .deleteAllByMemberId(
                            member.getId()
                    );

            throw new IllegalArgumentException(
                    "인증 시간이 만료되었습니다. 다시 인증해 주세요."
            );
        }

        if (!token.isVerified()) {
            throw new IllegalArgumentException(
                    "비밀번호 재설정 인증이 필요합니다."
            );
        }

        if (!passwordEncoder.matches(
                verificationCode,
                token.getCodeHash()
        )) {
            throw new IllegalArgumentException(
                    "비밀번호 재설정 인증이 필요합니다."
            );
        }

        member.changePassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        passwordResetTokenRepository
                .deleteAllByMemberId(
                        member.getId()
                );
    }

    private String generateVerificationCode() {
        int code =
                secureRandom.nextInt(
                        CODE_BOUND
                ) + CODE_OFFSET;

        return String.valueOf(code);
    }
}