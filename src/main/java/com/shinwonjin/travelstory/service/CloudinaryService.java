package com.shinwonjin.travelstory.service;

import java.io.IOException;
import java.util.Map;
import java.net.MalformedURLException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public UploadResult uploadImage(
            MultipartFile file,
            String folder
    ) {
        try {
            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", folder,
                                    "resource_type", "image",
                                    "overwrite", false
                            )
                    );

            String secureUrl =
                    (String) result.get("secure_url");

            String publicId =
                    (String) result.get("public_id");

            if (
                    secureUrl == null
                    || secureUrl.isBlank()
                    || publicId == null
                    || publicId.isBlank()
            ) {
                throw new IllegalStateException(
                        "Cloudinary 이미지 정보를 확인할 수 없습니다."
                );
            }

            return new UploadResult(
                    secureUrl,
                    publicId
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cloudinary에 이미지를 업로드하지 못했습니다.",
                    exception
            );
        }
    }

    public void deleteImage(
            String publicId
    ) {
        if (
                publicId == null
                || publicId.isBlank()
        ) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cloudinary 이미지를 삭제하지 못했습니다.",
                    exception
            );
        }
    }

    public record UploadResult(
            String url,
            String publicId
    ) {
    }

    public Resource loadImage(
            String imageUrl
    ) {
        if (
                imageUrl == null
                || imageUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Cloudinary 이미지 정보를 찾을 수 없습니다."
            );
        }

        if (!imageUrl.startsWith("https://res.cloudinary.com/")) {
            throw new IllegalArgumentException(
                    "올바르지 않은 Cloudinary 이미지 경로입니다."
            );
        }

        try {
            return new UrlResource(imageUrl);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException(
                    "Cloudinary 이미지를 불러오지 못했습니다.",
                    exception
            );
        }
    }
}