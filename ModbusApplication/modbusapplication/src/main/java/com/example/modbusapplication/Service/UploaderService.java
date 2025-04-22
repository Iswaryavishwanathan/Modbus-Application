package com.example.modbusapplication.Service;

// import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Service
public class UploaderService {

    private final File file = new File("modbus-buffer.txt");
    private final String uploadUrl = "http://localhost:8082/api/upload-bytes";
    private final RestTemplate restTemplate = new RestTemplate();

    // @PostConstruct
    // public void init() {
    //     try {
    //         if (!file.exists()) {
    //             file.createNewFile();
    //             System.out.println("📄 Created buffer file: " + file.getAbsolutePath());
    //         }
    //     } catch (IOException e) {
    //         System.err.println("❌ Failed to create buffer file: " + e.getMessage());
    //     }
    // }

    // @Scheduled(fixedRate = 5000)
    public void uploadData() {
        try {
            List<String> base64Lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (base64Lines.isEmpty()) return;

            // Optional: remove empty lines
            base64Lines.removeIf(String::isBlank);

            if (base64Lines.isEmpty()) return;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Send raw list of strings as JSON
            HttpEntity<List<String>> request = new HttpEntity<>(base64Lines, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Files.write(file.toPath(), new byte[0]); // clear buffer
                System.out.println("✅ Uploaded " + base64Lines.size() + " raw byte records.");
            } else {
                System.out.println("⚠️ Upload failed with status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            System.err.println("⚠️ Upload error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during upload: " + e.getMessage());
        }
    }
}
