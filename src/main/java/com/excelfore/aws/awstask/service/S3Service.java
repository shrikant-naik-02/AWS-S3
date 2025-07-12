package com.excelfore.aws.awstask.service;

import com.excelfore.aws.awstask.common.CommonAWSOp;
import com.excelfore.aws.awstask.dto.PresignedUrlResponse;
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

    private static final String FOLDER_NAME = "myBucket/";

    public String uploadFile(MultipartFile file) {
        PresignedUrlResponse uploadUrl = getValidPresignedUrlForUpload(file);
        return uploadFileWithPresign(file, uploadUrl.getUrl());
    }

    public byte[] downloadFile(String objectName) {
        PresignedUrlResponse downloadUrl = getValidPresignedUrlForDownload(objectName);
        return downloadFileWithPresign(downloadUrl.getUrl());
    }

//    ------------------------------------------------------------------------------------------------------------------

    private PresignedUrlResponse getValidPresignedUrlForUpload(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw new EmptyFileException("File is null or empty");
        }

        if (file.getSize() > FileUtil.mbToBytes(1)) {
            throw new FileTooLargeException("File size exceeds 1MB limit");
        }

        String originalFileName = file.getOriginalFilename();
        assert originalFileName != null;
        if (!FileUtil.isFileNameValid(originalFileName)) {
            throw new InvalidFileNameException("Filename must contain at least one letter before the extension");
        }

        String hashHex = FileUtil.computeSHA256Hash(file);
        String key = FOLDER_NAME + hashHex;
        log.debug("Generated S3 object key: {}", key);

        if (commonAWSOp.doesObjectExists(key)) {
            throw new FileAlreadyExistsException("File already exists with name: " + key);
        }

        String url = commonAWSOp.generatePresignedUrl(key, true);
        return new PresignedUrlResponse("Upload", url, "5Min", true);
    }

    private String uploadFileWithPresign(MultipartFile file, String presignedUrl) {
        String currentFileHash = FileUtil.computeSHA256Hash(file);
        Map<String, String> urlData = FileUtil.extractFolderShaKeyAndObjName(presignedUrl);
        final String shaKey = urlData.get("shaKey");
        final String objName = urlData.get("objName");

        if (!currentFileHash.equalsIgnoreCase(shaKey)) {
            log.debug("mismatch");
            throw new HashMismatchException("Hash mismatch — please upload the original file used to generate the URL.");
        }

        if (commonAWSOp.doesObjectExists(objName)) {
            throw new FileAlreadyExistsException("File already exists with name: " + objName);
        }

        commonAWSOp.uploadFileWithPresignedUrl(file, presignedUrl);
        return objName;
    }

    private PresignedUrlResponse getValidPresignedUrlForDownload(String objectName) {
        if (!commonAWSOp.doesObjectExists(objectName)) {
            log.debug("File Not Exist There With Name {}", objectName);
            throw new FileAlreadyExistsException("File Not Present There with name: " + objectName);
        }

        String url = commonAWSOp.generatePresignedUrl(objectName, false);
        return new PresignedUrlResponse("Download", url, "5Min", true);
    }

    private byte[] downloadFileWithPresign(String presignedUrl) {
        Map<String, String> urlData = FileUtil.extractFolderShaKeyAndObjName(presignedUrl);
        final String objName = urlData.get("objName");

        if (!commonAWSOp.doesObjectExists(objName)) {
            log.debug("File Already There With Name {}", objName);
            throw new NoSuchFilePresent("File Not exists with name: " + objName);
        }

        return commonAWSOp.downloadFileWithPresignedUrl(presignedUrl);
    }


}
