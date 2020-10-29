package ru.yandex.cloud.graphql.gateway.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import ru.yandex.cloud.graphql.gateway.configuration.model.FileLocation;

@RequiredArgsConstructor
public class FileLoader {

    private final S3Client s3Client;

    public String readFile(FileLocation location) {
        try {
            String path = location.getPath();
            if (path != null) {
                return Files.exists(Paths.get(path)) ?
                        String.join("\n", Files.readAllLines(Paths.get(path))) :
                        Resources.toString(Resources.getResource(path), Charsets.UTF_8);
            } else {
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(location.getBucket())
                        .key(location.getKey())
                        .build();
                return s3Client.getObjectAsBytes(request).asUtf8String();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
