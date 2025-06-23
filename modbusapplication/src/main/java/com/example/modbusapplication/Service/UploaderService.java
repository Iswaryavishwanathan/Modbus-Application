package com.example.modbusapplication.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.modbusapplication.Model.RawRecordDTO;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploaderService {
    private static final String FILE_PATH = "modbus-buffer.txt";
    private static final String TEMP_FILE_PATH = "temp.txt";
    // private static final String UPLOAD_URL = "http://localhost:8082/api/upload-bytes";
    private static final String UPLOAD_URL = "http://13.202.101.254:8082/modbus/upload-bytes";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final RestTemplate restTemplate = new RestTemplate();

    public void uploadData() {
        try {
            List<RawRecordDTO> rawRecordDTOs = readFileAndPrepareData();
            if (rawRecordDTOs.isEmpty()) {
                System.out.println("No data to upload.");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<RawRecordDTO>> request = new HttpEntity<>(rawRecordDTOs, headers);

            // Send data to server
            ResponseEntity<String> response = sendDataToServer(request, 0);

            if (response.getStatusCode() == HttpStatus.OK) {
                // If the upload was successful, delete the lines
                deleteLinesInFile(rawRecordDTOs.size());
            } else {
                System.out.println("Upload failed with status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            System.err.println("Upload error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(" Unexpected error during upload: " + e.getMessage());
        }
    }

    // Retry mechanism added here
    private ResponseEntity<String> sendDataToServer(HttpEntity<List<RawRecordDTO>> request, int attempt) {
        if (attempt >= MAX_RETRY_ATTEMPTS) {
            System.out.println("Max retry attempts reached.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            return restTemplate.postForEntity(UPLOAD_URL, request, String.class);
        } catch (Exception e) {
            System.out.println(" Retry attempt " + (attempt + 1) + " failed. Retrying...");
            return sendDataToServer(request, attempt + 1); // Retry recursively
        }
    }

    private List<RawRecordDTO> readFileAndPrepareData() {
        List<RawRecordDTO> rawRecordDTOs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String encByteSTring;
            while ((encByteSTring = reader.readLine()) != null) {
                rawRecordDTOs.add(new RawRecordDTO(encByteSTring));
            }
        } catch (IOException e) {
            System.out.println(" Error reading file: " + e.getMessage());
        }
        return rawRecordDTOs;
    }

    private void deleteLinesInFile(int linesToDelete) throws IOException {
        Path inputFilePath = Paths.get(FILE_PATH);
        Path tempFilePath = Paths.get(TEMP_FILE_PATH);

        try (BufferedReader reader = Files.newBufferedReader(inputFilePath);
             BufferedWriter writer = Files.newBufferedWriter(tempFilePath)) {

            // Skip the first 'linesToDelete' lines
            for (int i = 0; i < linesToDelete; i++) {
                reader.readLine();
            }

            // Write the remaining lines to the temp file
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }

        // Replace the original file with the temp file
        Files.delete(inputFilePath);
        Files.move(tempFilePath, inputFilePath);
        System.out.println(" Lines deleted and file updated.");
    }
}
