package com.excelfore.aws.awstask.common;

import com.excelfore.aws.awstask.exception.EmptyFileException;
import com.excelfore.aws.awstask.exception.PresignedUrlExpiredException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonAWSOp {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public String generatePresignedUrl(String key, boolean isUpload) {
        Duration expiration = Duration.ofMinutes(5);

        if (isUpload) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                    PutObjectPresignRequest.builder()
                            .putObjectRequest(putRequest)
                            .signatureDuration(expiration)
                            .build()
            );

            return presignedRequest.url().toString();

        } else {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentDisposition("attachment; filename=\"" + key + "\"")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .getObjectRequest(getRequest)
                            .signatureDuration(expiration)
                            .build()
            );

            return presignedRequest.url().toString();
        }
    }

    public void uploadFileWithPresignedUrl(MultipartFile file, String presignedUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(presignedUrl))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .header("Content-Type", file.getContentType())
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            log.debug("Upload response status: {}", statusCode);

            if (statusCode == 200) return;

            if (statusCode == 403) {
                log.warn("Presigned URL has expired or is invalid.");
                throw new PresignedUrlExpiredException("Presigned URL has expired or is invalid.");
            }

            throw new RuntimeException("Upload failed. HTTP status: " + statusCode);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Upload interrupted: {}", e.getMessage());
            throw new RuntimeException("Upload was interrupted", e);

        } catch (IOException e) {
            log.error("I/O error during upload: {}", e.getMessage());
            throw new RuntimeException("Upload failed due to I/O error", e);
        }
    }

    public byte[] downloadFileWithPresignedUrl(String presignedUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(presignedUrl))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int statusCode = response.statusCode();

            log.debug("Download response status: {}", statusCode);

            if (statusCode == 403) {
                log.warn("Presigned URL has expired or is invalid.");
                throw new PresignedUrlExpiredException("Presigned URL has expired or was manipulated.");
            }

            if (statusCode != 200) {
                log.error("Download failed. HTTP status: {}", statusCode);
                throw new RuntimeException("Download failed. HTTP status: " + statusCode);
            }

            byte[] fileBytes = response.body();
            if (fileBytes.length == 0) {
                log.warn("Downloaded file is empty.");
                throw new EmptyFileException("Downloaded file is empty.");
            }

            return fileBytes;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Download interrupted: {}", e.getMessage());
            throw new RuntimeException("Download was interrupted", e);

        } catch (IOException e) {
            log.error("I/O error during download: {}", e.getMessage());
            throw new RuntimeException("Download failed due to I/O error", e);
        }
    }

    public boolean doesObjectExist(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;

        } catch (NoSuchKeyException e) {
            log.debug("Object not found: {} | StatusCode: {} | RequestId: {}", key, e.statusCode(), e.requestId());
            return false;

        } catch (S3Exception e) {
            log.error("S3 error while checking object existence: {} | StatusCode: {} | RequestId: {}", e.awsErrorDetails().errorMessage(), e.statusCode(), e.requestId());
            throw new RuntimeException("S3 error: " + e.awsErrorDetails().errorMessage());
        }
    }
}
