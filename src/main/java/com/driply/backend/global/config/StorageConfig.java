package com.driply.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class StorageConfig {

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileLocation;

    @Bean
    public Storage storage() throws IOException {
        InputStream keyFile = new ClassPathResource(keyFileLocation).getInputStream();
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();
    }
}