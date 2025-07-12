package com.excelfore.aws.awstask.controller;

import com.excelfore.aws.awstask.dto.ApiResponse;
import com.excelfore.aws.awstask.dto.PresignedUrlResponse;
import com.excelfore.aws.awstask.service.S3ServiceV2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/s3_bucket")
public class S3ControllerV2 {

    private final S3ServiceV2 s3ServiceV2;

    @PostMapping("/upload-file-using-presigned-url")
    public ResponseEntity<ApiResponse<String>> uploadFileUsingPresignedUrl(
            @RequestParam("file") MultipartFile file,
            @RequestParam("presignedUrl") String presignedUrl) {

        String objName = s3ServiceV2.uploadFileWithPresign(file, presignedUrl);
        return ResponseEntity.ok(new ApiResponse<>("File " + objName + " Uploaded Successfully"));
    }

    @PostMapping("/download-file-using-presigned-url")
    public ResponseEntity<byte[]> downloadFileUsingPresigned(@RequestParam("presignedUrl") String presignedUrl) {
        log.debug("Downloading from presigned URL: {}", presignedUrl);

        byte[] fileBytes = s3ServiceV2.downloadFileWithPresign(presignedUrl);
        return ResponseEntity.ok().body(fileBytes);
    }

}
