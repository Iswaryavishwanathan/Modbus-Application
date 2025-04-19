package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploaderService {

    private final File file = new File("modbus-buffer.txt");
    private final String uploadUrl = "http://localhost:8080/api/upload"; // your actual URL
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final int REGISTER_COUNT = 10;

    @PostConstruct
    public void init() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create file: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void uploadData() {
        try {
            byte[] data = Files.readAllBytes(file.toPath());

            if (data.length == 0) return;

            List<ModbusRecord> records = new ArrayList<>();

            for (int i = 0; i < data.length; ) {
                ByteArrayInputStream bais = new ByteArrayInputStream(data, i, data.length - i);
                DataInputStream dis = new DataInputStream(bais);
            
                long timestamp = dis.readLong();
            
                int nameLength = dis.readInt();
                byte[] nameBytes = new byte[nameLength];
                dis.readFully(nameBytes);
                String name = new String(nameBytes, "UTF-8");
            
                List<Integer> values = new ArrayList<>();
                for (int j = 0; j < REGISTER_COUNT; j++) {
                    values.add(dis.readShort() & 0xFFFF);
                }
            
                records.add(new ModbusRecord(timestamp, name, values));
            
                i += 8 + 4 + nameLength + REGISTER_COUNT * 2; // timestamp + nameLength info + name + register data
            }
            

            if (!records.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String json = mapper.writeValueAsString(records);
                HttpEntity<String> request = new HttpEntity<>(json, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, request, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    Files.write(file.toPath(), new byte[0]); // clear the file
                    System.out.println("✅ Uploaded " + records.size() + " records and cleared the file.");
                } else {
                    System.out.println("❌ Upload failed with status: " + response.getStatusCode());
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error during upload: " + e.getMessage());
        }
    }
}
