package dev.itsdaksh.controlplane.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.getUrl())
                .credentials(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                )
                .build();
        try {
            boolean found = client.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(properties.getBucket())
                            .build()
            );
            if (!found) {
                client.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(properties.getBucket())
                                .build()
                );
            }
        } catch (Exception e) {
            System.err.println("Error initializing MinIO bucket: " + e.getMessage());
        }
        return client;
    }
}