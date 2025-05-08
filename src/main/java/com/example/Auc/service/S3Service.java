//package com.example.Auc.service;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.InputStream;
//
//@Service
//public class S3Service {
//    private final AmazonS3 s3Client;
//    @Value("${aws.s3.bucket}")
//    private String bucketName;
//
//    public S3Service(AmazonS3 s3Client) {
//        this.s3Client = s3Client;
//    }
//
//    public String uploadFile(MultipartFile file) throws Exception {
//        String key = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        InputStream is = file.getInputStream();
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(file.getSize());
//        s3Client.putObject(bucketName, key, is, metadata);
//        return s3Client.getUrl(bucketName, key).toString();
//    }
//}