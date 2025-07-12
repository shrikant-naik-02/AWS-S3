package com.excelfore.aws.awstask.controller;

import com.excelfore.aws.awstask.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/s3")
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        String objName = s3Service.uploadFile(file);
        return "File '" + objName + "' uploaded successfully";
    }

    @GetMapping("/download")
    public byte[] downloadFile(@RequestParam("objectName") String objectName) {
        log.debug("Downloading file: {}", objectName);
        return s3Service.downloadFile(objectName);
    }

}
