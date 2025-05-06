
package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.SystemConfig;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
public class ModbusService {

    private String plcIp;
    private int plcPort;
    private String deviceId;
    // private String deviceId = "RICE-MILL-01"; 
    private final String filePath = "modbus-buffer.txt";
    // private final String configFilePath = "modbus-config.txt";  // Path to the config file
    int m =10;
    


    public ModbusService() {
        loadConfig();  // Load the configuration from file
    }

    // Load the IP address and port from the configuration file
    private void loadConfig() {
        System.out.println("entered loadCOnfig");
        try{
            SystemConfigService systemConfigService = new SystemConfigService();
            SystemConfig systemConfig = systemConfigService.readFromFile();
            // Read the IP and Port values from the config file
            plcIp =  systemConfig.getIpAddress_plc(); // Default value if not found
            plcPort = Integer.parseInt(systemConfig.getPort_plc()); 
            

            System.out.println("Loaded Config - IP: " + plcIp + ", Port: " + plcPort);

        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void readModbusRegisters() {
        SystemConfigService systemConfigService = new SystemConfigService();
        SystemConfig systemConfig = systemConfigService.readFromFile();
        TCPMasterConnection connection = null;
    
        while (true) {
            try {
                System.out.println("Trying to connect to PLC at IP: " + plcIp + ", Port: " + plcPort);
                connection = createConnection(plcIp);
                connection.connect();
    
                if (!connection.isConnected()) {
                    System.err.println("Initial connection failed, reloading config...");
                    loadConfig();  // Try updated config
                    connection = createConnection(plcIp);
                    connection.connect();
                }
    
                if (!connection.isConnected()) {
                    throw new IOException("Unable to connect to PLC at IP: " + plcIp);
                }
    
                System.out.println("✅ Connected to PLC at IP: " + plcIp);
    
                // Execute Modbus read transaction
                ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(0, 10);
                request.setUnitID(1);
    
                ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
                transaction.setRequest(request);
                transaction.execute();
    
                ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
    
                List<Integer> registerValues = new ArrayList<>();
                for (int i = 0; i < response.getWordCount(); i++) {
                    registerValues.add(response.getRegister(i).getValue());
                }
    
                byte[] byteArray = new byte[16];
                int index = 0;
                for (int i = 2; i <= 9; i++) {
                    byteArray[index++] = (byte) (registerValues.get(i) & 0xFF);
                    byteArray[index++] = (byte) ((registerValues.get(i) >> 8) & 0xFF);
                }
    
                String batchName = new String(byteArray, StandardCharsets.UTF_8).trim();
                System.out.println("Machine Name from HMI: " + batchName);
    
                deviceId = systemConfig.getDeviceName();
                System.out.println("Device ID: " + deviceId);
    
                ZoneId zone = ZoneId.systemDefault();
                LocalDateTime now = LocalDateTime.now(zone);
                String localTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
                List<ModbusRecord> recodsList = new ArrayList<>();
                recodsList.add(new ModbusRecord("datetime", localTime));
                recodsList.add(new ModbusRecord("batchName", batchName));
                recodsList.add(new ModbusRecord("setWeight", "" + registerValues.get(0)));
                recodsList.add(new ModbusRecord("actualWeight", "" + registerValues.get(1)));
                recodsList.add(new ModbusRecord("deviceId", deviceId));
    
                ByteString byteString = toByteString(recodsList);
    
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    writer.write(Base64.getEncoder().encodeToString(byteString.toByteArray()));
                    writer.newLine();
                    System.out.println("✅ Wrote records as ByteString to file.");
                } catch (IOException e) {
                    System.err.println("❌ Error writing to file: " + e.getMessage());
                    e.printStackTrace();
                }
    
                break;  // ✅ Exit the retry loop after successful read
    
            } catch (Exception e) {
                System.err.println("❌ Modbus error: " + e.getMessage());
    
                try {
                    if (connection != null) connection.close();
                    System.out.println("🔁 Retrying in 2 seconds...");
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;  // Exit if interrupted externally
                } catch (Exception ignored) {}
            }
        }
    }
    
    public void increament(){
        System.out.println(m);
        m+=1;
    }

    // Create Modbus connection
    private TCPMasterConnection createConnection(String ip) throws IOException {
        InetAddress address = InetAddress.getByName(ip);
        TCPMasterConnection connection = new TCPMasterConnection(address);
        connection.setPort(plcPort);
        return connection;
    }

    // Convert list of objects to ByteString
    public static ByteString toByteString(List<ModbusRecord> list) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(list);
        }
        return ByteString.copyFrom(bos.toByteArray());
    }
    public int peekActualWeight() {
        try {
            int[] registers = readRegistersRaw();
            if (registers.length > 1) {
                return registers[1]; // actual weight assumed at index 1
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Error peeking weight: " + e.getMessage());
            return -1;
        }
    }
    
    public int[] readRegistersRaw() {
        TCPMasterConnection connection = null;
        try {
            connection = createConnection(plcIp);
            connection.connect();
    
            if (!connection.isConnected()) {
                loadConfig();
                connection = createConnection(plcIp);
                connection.connect();
            }
    
            if (!connection.isConnected()) {
                throw new IOException("Unable to connect to PLC at IP: " + plcIp);
            }
    
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(0, 10);
            request.setUnitID(1);
    
            ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();
    
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            int[] registers = new int[response.getWordCount()];
            for (int i = 0; i < response.getWordCount(); i++) {
                registers[i] = response.getRegister(i).getValue();
            }
    
            return registers;
        } catch (Exception e) {
            System.err.println("readRegistersRaw :: Modbus error :: " + e.getMessage());
            return new int[0];  // Return empty array if error
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }
    
    
}
