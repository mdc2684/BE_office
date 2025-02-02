package com.team11.shareoffice.post.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //파일을 s3에 업로드
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        validateFileExists(multipartFile);

        String fileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        log.info(fileName);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());

        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

//    public boolean delete(String fileUrl) {
//        try {
//            String[] temp = fileUrl.split("_");
//            String fileKey = temp[temp.length - 1];
//            amazonS3Client.deleteObject(bucket, fileKey);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public void delete(String fileName){
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    //파일 유 / 무 확인 메서드
    private void validateFileExists(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }
}
