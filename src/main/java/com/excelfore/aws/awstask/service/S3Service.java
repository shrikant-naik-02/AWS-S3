package com.excelfore.aws.awstask.service;

import com.excelfore.aws.awstask.common.CommonAWSOp;
import com.excelfore.aws.awstask.exception.*;
import com.excelfore.aws.awstask.util.FileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final CommonAWSOp commonAWSOp;
    private static final String S3_FOLDER_PREFIX = "myBucket/";

    public String uploadFile(MultipartFile file) {
        String presignedUrl = generatePresignedUrlForUpload(file);
        return uploadFileWithPresignedUrl(file, presignedUrl);
    }

    public byte[] downloadFile(String objectKey) {
        String presignedUrl = generatePresignedUrlForDownload(objectKey);
        return downloadFileWithPresignedUrl(presignedUrl);
    }

    // ---------------------------------------------------------------------------

    private String generatePresignedUrlForUpload(MultipartFile file) {

        if (file.getSize() > FileUtil.mbToBytes(1)) {
            throw new FileTooLargeException("File size exceeds 1MB limit");
        }

        String fileHash = FileUtil.computeSHA256Hash(file);
        String objectKey = S3_FOLDER_PREFIX + fileHash;
        log.debug("Generated S3 object key: {}", objectKey);

        if (commonAWSOp.doesObjectExist(objectKey)) {
            throw new FileAlreadyExistsException("File already exists with key: " + objectKey);
        }

        return commonAWSOp.generatePresignedUrl(objectKey, true); // true = upload
    }

    private String uploadFileWithPresignedUrl(MultipartFile file, String presignedUrl) {

        Map<String, String> parsedData = FileUtil.extractFolderShaKeyAndObjName(presignedUrl);

        String objectKey = parsedData.get("objName");

        if (commonAWSOp.doesObjectExist(objectKey)) {
            throw new FileAlreadyExistsException("File already exists with key: " + objectKey);
        }

        commonAWSOp.uploadFileWithPresignedUrl(file, presignedUrl);
        return objectKey;
    }

    private String generatePresignedUrlForDownload(String objectKey) {
        if (!commonAWSOp.doesObjectExist(objectKey)) {
            log.debug("File not found with key: {}", objectKey);
            throw new FileAlreadyExistsException("File not present with key: " + objectKey);
        }

        return commonAWSOp.generatePresignedUrl(objectKey, false); // false = download
    }

    private byte[] downloadFileWithPresignedUrl(String presignedUrl) {
        Map<String, String> parsedData = FileUtil.extractFolderShaKeyAndObjName(presignedUrl);
        String objectKey = parsedData.get("objName");

        if (!commonAWSOp.doesObjectExist(objectKey)) {
            log.debug("File not found with key: {}", objectKey);
            throw new NoSuchFilePresent("File not found with key: " + objectKey);
        }

        return commonAWSOp.downloadFileWithPresignedUrl(presignedUrl);
    }
}
