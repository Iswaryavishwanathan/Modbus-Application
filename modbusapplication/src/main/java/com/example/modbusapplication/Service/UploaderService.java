package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusData;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;

import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class UploaderService {

    private final File file = new File("modbus-buffer.txt");
    private final String uploadUrl = "http://localhost:8080/api/upload"; // change this to your actual URL
    private final RestTemplate restTemplate = new RestTemplate();

    private final int REGISTER_COUNT = 10;
    private final int RECORD_SIZE = 8 + REGISTER_COUNT * 2; // 8 bytes timestamp + 20 bytes data = 28 bytes

    @PostConstruct
    public void init() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            System.err.println("Failed to create file: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
public void uploadData() {
    try {
        byte[] fileData = Files.readAllBytes(file.toPath());

        if (fileData.length > 0) {
            // Set headers manually
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> request = new HttpEntity<>(fileData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Files.write(file.toPath(), new byte[0]); // clear file
                System.out.println("✅ Binary data uploaded and file cleared");
            } else {
                System.out.println("❌ Upload failed with status: " + response.getStatusCode());
            }
        }

    } catch (Exception e) {
        System.err.println("⚠️ Error during binary upload: " + e.getMessage());
    }
}


    // @Scheduled(fixedRate = 5000)
    // public void uploadData() {
    //     try {
    //         byte[] allBytes = Files.readAllBytes(file.toPath());

    //         if (allBytes.length >= RECORD_SIZE) {
    //             ByteArrayInputStream bais = new ByteArrayInputStream(allBytes);
    //             DataInputStream dis = new DataInputStream(bais);

    //             // Read timestamp
    //             long timestampMillis = dis.readLong();

    //             // Read register values (shorts)
    //             int[] registerValues = new int[REGISTER_COUNT];
    //             for (int i = 0; i < REGISTER_COUNT; i++) {
    //                 registerValues[i] = dis.readShort() & 0xFFFF; // Ensure unsigned
    //             }

    //             // Format timestamp to readable string
    //             String formattedTimestamp = Instant.ofEpochMilli(timestampMillis)
    //                     .atZone(ZoneId.systemDefault())
    //                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    //             // Build ModbusData object
    //             ModbusData data = new ModbusData();
    //             data.setValues(registerValues);
    //             data.setTimestamp(formattedTimestamp);

    //             // Send it via HTTP
    //             HttpEntity<ModbusData> request = new HttpEntity<>(data);
    //             ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, request, String.class);

    //             if (response.getStatusCode() == HttpStatus.OK) {
    //                 // Remove first record bytes
    //                 byte[] remainingBytes = Arrays.copyOfRange(allBytes, RECORD_SIZE, allBytes.length);
    //                 Files.write(file.toPath(), remainingBytes);
    //                 System.out.println("✅ Uploaded and removed 1 binary record");
    //             } else {
    //                 System.out.println("❌ Upload failed: " + response.getStatusCode());
    //             }
    //         }

    //     } catch (Exception e) {
    //         System.err.println("⚠️ Error during binary upload: " + e.getMessage());
    //     }
    // }
}


// package com.example.modbusapplication.Service;

// import com.example.modbusapplication.Model.ModbusData;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.annotation.PostConstruct;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.io.File;
// import java.nio.file.Files;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class UploaderService {

//     private final ObjectMapper mapper = new ObjectMapper();
//     private final File file = new File("modbus-buffer.txt");
//     private final String uploadUrl = "http://your-server.com/api/upload"; // change this to your actual URL

//     private final RestTemplate restTemplate = new RestTemplate();

//     @PostConstruct
//     public void init() {
//         try {
//             if (!file.exists()) {
//                 file.createNewFile();
//             }
//         } catch (Exception e) {
//             System.err.println("Failed to create file: " + e.getMessage());
//         }
//     }

//     @Scheduled(fixedRate = 5000)
//     public void uploadData() {
//         try {
//             List<String> lines = Files.readAllLines(file.toPath());

//             if (!lines.isEmpty()) {
//                 String json = lines.get(0);

//                 // ✅ Parse JSON to ModbusData object to ensure it's valid
//                 ModbusData data = mapper.readValue(json, ModbusData.class);

//                 // Wrap the object in HttpEntity for POST request
//                 HttpEntity<ModbusData> request = new HttpEntity<>(data);

//                 // ✅ Send the object instead of raw JSON
//                 ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, request, String.class);

//                 if (response.getStatusCode() == HttpStatus.OK) {
//                     // Remove first line if upload succeeded
//                     List<String> updated = lines.stream().skip(1).collect(Collectors.toList());
//                     Files.write(file.toPath(), updated);
//                     System.out.println("✅ Uploaded and removed: " + json);
//                 } else {
//                     System.out.println("❌ Upload failed with status: " + response.getStatusCode());
//                 }
//             }

//         } catch (Exception e) {
//             System.err.println("⚠️ Error during upload: " + e.getMessage());
//         }
//     }
// }


