package com.shinwonjin.travelstory.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GmailApiService {

    private static final String TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String SEND_URL =
            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String senderEmail;

    public GmailApiService(
            @Value("${gmail.client-id}")
            String clientId,

            @Value("${gmail.client-secret}")
            String clientSecret,

            @Value("${gmail.refresh-token}")
            String refreshToken,

            @Value("${gmail.sender-email}")
            String senderEmail
    ) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.senderEmail = senderEmail;
    }

    public void sendTextEmail(
            String recipient,
            String subject,
            String body
    ) {
        String accessToken =
                getAccessToken();

        String rawMessage =
                createRawMessage(
                        recipient,
                        subject,
                        body
                );

        try {
            restClient
                    .post()
                    .uri(SEND_URL)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .body(
                            Map.of(
                                    "raw",
                                    rawMessage
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();
        } catch (
                RestClientResponseException exception
        ) {
            throw new IllegalStateException(
                    "Gmail API를 통해 메일을 전송하지 못했습니다.",
                    exception
            );
        }
    }

    private String getAccessToken() {
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add(
                "client_id",
                clientId
        );

        form.add(
                "client_secret",
                clientSecret
        );

        form.add(
                "refresh_token",
                refreshToken
        );

        form.add(
                "grant_type",
                "refresh_token"
        );

        try {
            Map<?, ?> response =
                    restClient
                            .post()
                            .uri(TOKEN_URL)
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body(form)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Google OAuth 응답을 확인할 수 없습니다."
                );
            }

            Object accessToken =
                    response.get(
                            "access_token"
                    );

            if (
                    accessToken == null
                    || accessToken
                            .toString()
                            .isBlank()
            ) {
                throw new IllegalStateException(
                        "Google OAuth Access Token을 발급받지 못했습니다."
                );
            }

            return accessToken.toString();
        } catch (
                RestClientResponseException exception
        ) {
            throw new IllegalStateException(
                    "Google OAuth Access Token을 발급받지 못했습니다.",
                    exception
            );
        }
    }

    private String createRawMessage(
            String recipient,
            String subject,
            String body
    ) {
        String encodedSubject =
                Base64.getEncoder()
                        .encodeToString(
                                subject.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String encodedBody =
                Base64.getMimeEncoder(
                                76,
                                "\r\n".getBytes(
                                        StandardCharsets.US_ASCII
                                )
                        )
                        .encodeToString(
                                body.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String message =
                "From: Travel Story <"
                        + senderEmail
                        + ">\r\n"
                        + "To: "
                        + recipient
                        + "\r\n"
                        + "Subject: =?UTF-8?B?"
                        + encodedSubject
                        + "?=\r\n"
                        + "MIME-Version: 1.0\r\n"
                        + "Content-Type: text/plain; charset=UTF-8\r\n"
                        + "Content-Transfer-Encoding: base64\r\n"
                        + "\r\n"
                        + encodedBody;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        message.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }
}