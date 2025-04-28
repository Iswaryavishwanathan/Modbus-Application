package com.example.modbusapplication.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.SystemConfig;
import com.google.protobuf.ByteString;

@Service
public class SystemConfigService {
    
    private String filePath = "system-configuration.txt";

    // Method to save SystemConfig to file
    public boolean saveToFiles(SystemConfig systemConfig) {
        try {
            // Convert SystemConfig to ByteString
            
            SystemConfig systemConfigExcistingData = readFromFile();
            deleteFile();
            updateSystemConfig(systemConfig, systemConfigExcistingData);
            ByteString byteString = toByteString(systemConfig);
            // Write the Base64-encoded ByteString to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(Base64.getEncoder().encodeToString(byteString.toByteArray()));
                writer.newLine();  // Add newline for each entry
                System.out.println("Wrote record in system-configuration.txt");
                return true;
            } catch (IOException e) {
                System.err.println("saveToFiles :: Error writing to file :: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("Exception :: saveToFiles ::" + e);
        }
        return false;
    }

    // Method to read SystemConfig from file
    public SystemConfig readFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String encByteString = reader.readLine(); // Read the first line (latest config)
            
            if (encByteString != null) {
                // Decode the Base64 string back into a byte array
                byte[] data = Base64.getDecoder().decode(encByteString);
                
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                    // Deserialize the byte array back into a SystemConfig object
                    Object obj = ois.readObject();
                    if (obj instanceof SystemConfig) {
                        return (SystemConfig) obj;
                    } else {
                        System.out.println("⚠️ Deserialized object is not of type SystemConfig.");
                    }
                }
            } else {
                System.out.println("⚠️ File is empty.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("⚠️ Error reading file: " + e.getMessage());
        }
        return new SystemConfig("", "", "", "", "","");
    }

    public void updateSystemConfig(SystemConfig systemConfig,SystemConfig systemConfigExcistingData){
        if(systemConfig.getIpAddress_plc() == null || systemConfig.getIpAddress_plc() ==""){
            systemConfig.setIpAddress_plc(systemConfigExcistingData.getIpAddress_plc());
        }
        if(systemConfig.getPort_plc() == null || systemConfig.getPort_plc() ==""){
            systemConfig.setPort_plc(systemConfigExcistingData.getPort_plc());
        }
        if(systemConfig.getIpaddress_rashpi() == null || systemConfig.getIpaddress_rashpi() ==""){
            systemConfig.setIpaddress_rashpi(systemConfigExcistingData.getIpaddress_rashpi());
        }
        if(systemConfig.getWifiName() == null || systemConfig.getWifiName() ==""){
            systemConfig.setWifiName(systemConfigExcistingData.getWifiName());
        }
        if(systemConfig.getWifiPassword() == null || systemConfig.getWifiPassword() ==""){
            systemConfig.setWifiPassword(systemConfigExcistingData.getWifiPassword());
        }
        if (systemConfig.getDeviceName() == null || systemConfig.getDeviceName()=="") {
            systemConfig.setDeviceName(systemConfigExcistingData.getDeviceName());
        }
       

    }

    // Convert SystemConfig object to ByteString (for serialization)
    public static ByteString toByteString(SystemConfig systemConfig) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            // Serialize the SystemConfig object
            oos.writeObject(systemConfig);
        }
        // Convert the serialized byte array into ByteString
        return ByteString.copyFrom(bos.toByteArray());
    }

    // Method to delete the configuration file
    public boolean deleteFile() {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully.");
                return true;
            } else {
                System.out.println("⚠️ Failed to delete the file.");
            }
        } else {
            System.out.println("⚠️ File does not exist.");
        }
        return false;
    }
    public boolean updateWifiNetplan(SystemConfig systemConfig) {
        try {
            String netplanConfig =
                "network:\n" +
                "    version: 2\n" +
                "    wifis:\n" +
                "        renderer: networkd \n" +
                "        wlan0:\n" +
                // "      dhcp4: no\n" +
                // "      addresses:\n" +
                // "        - " + systemConfig.getIpaddress_rashpi() + "/24\n" +
                // "      gateway4: 192.168.1.1\n" +
                // "      nameservers:\n" +
                // "        addresses:\n" +
                // "          - 8.8.8.8\n" +
                // "          - 8.8.4.4\n" +
                "            access-points:\n" +
                "                "+ systemConfig.getWifiName() + ":\n" +
                "                    password: " + systemConfig.getWifiPassword() + "\n"  +
                "            dhcp4: true \n" +
                "            optional: true";
                
    
            
            String path = "./mock-netplan.yaml";
            Files.write(Paths.get(path), netplanConfig.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    
            System.out.println("✅ (Test) Mock Netplan Wi-Fi + Static IP config updated at mock-netplan.yaml.");
    
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error updating Mock Netplan config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
}
