package com.groupproject.ecommerce.service.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void uploadFile(String objectName, MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created bucket: {}", bucketName);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Uploaded file: {} to bucket: {}", objectName, bucketName);
        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage());
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Error getting file from MinIO", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("Deleted file: {} from bucket: {}", objectName, bucketName);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Error deleting file from MinIO", e);
        }
    }
}
