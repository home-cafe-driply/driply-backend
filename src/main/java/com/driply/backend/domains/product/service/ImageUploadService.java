package com.driply.backend.domains.product.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;

    /**
     * 이미지 파일 업로드
     */
    public String uploadImage(MultipartFile file, String folder) {
        // 파일 검증
        validateImageFile(file);

        // 유니크 파일명 생성
        String fileName = generateUniqueFileName(file.getOriginalFilename(), folder);

        try {
            // GCP Storage에 업로드
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // 공개 URL 반환
            String imageUrl = String.format("https://storage.googleapis.com/%s/%s",
                    bucketName, fileName);

            log.info("이미지 업로드 성공: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다", e);
        }
    }

    /**
     * 이미지 파일 삭제
     */
    public void deleteImage(String imageUrl) {
        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(imageUrl);

            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("이미지 삭제 성공: {}", fileName);
            } else {
                log.warn("이미지 삭제 실패 - 파일 없음: {}", fileName);
            }

        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 삭제에 실패했습니다", e);
        }
    }

    /**
     * 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다");
        }

        // 파일 크기 체크 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다");
        }

        // 이미지 타입 체크
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }

        // 지원하는 확장자 체크
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
        boolean isAllowed = Arrays.stream(allowedTypes)
                .anyMatch(type -> type.equals(contentType));

        if (!isAllowed) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다 (JPG, PNG, WebP만 가능)");
        }
    }

    /**
     * 유니크 파일명 생성
     */
    private String generateUniqueFileName(String originalFileName, String folder) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        return String.format("%s/%s_%s%s", folder, timestamp, uniqueId, extension);
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String imageUrl) {
        // https://storage.googleapis.com/bucket-name/folder/filename.jpg
        String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);

        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }

        throw new IllegalArgumentException("잘못된 이미지 URL 형식입니다");
    }
}