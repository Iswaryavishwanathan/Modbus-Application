// package com.example.modbusapplication.Service;

// import com.example.modbusapplication.Model.ModbusRecord;
// import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
// import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
// import com.google.protobuf.ByteString;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.time.ZoneId;
// import java.io.*;
// import java.net.InetAddress;
// import java.nio.charset.StandardCharsets;
// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.List;

// import org.springframework.stereotype.Service;

// @Service
// public class ModbusService  {

//     private final String plcIp = "192.168.1.1";
//     private final int plcPort = 502;
//     private final String filePath = "modbus-buffer.txt";

//     // @Scheduled(fixedRate = 10000)
//     public void readModbusRegisters() {
//         TCPMasterConnection connection = null;

//         try {
//             InetAddress address = InetAddress.getByName(plcIp);
//             connection = new TCPMasterConnection(address);
//             connection.setPort(plcPort);
//             connection.connect();

//             ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(0, 10);
//             request.setUnitID(1);

//             ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//             transaction.setRequest(request);
//             transaction.execute();

//             ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();

//             List<Integer> registerValues = new ArrayList<>();
//             for (int i = 0; i < response.getWordCount(); i++) {
//                 registerValues.add(response.getRegister(i).getValue());
//             }

//             // ✅ Extract machine name from registers 2 to 10 (9 registers = 18 bytes)
//             byte[] byteArray = new byte[16]; // 9 registers, each 2 bytes
//             int index = 0;

//             // Loop through registers 2 to 10 and get their low and high bytes
//             for (int i = 2; i <=9 ; i++) {
//                 byteArray[index++] = (byte) (registerValues.get(i) & 0xFF);         // Low byte
//                 byteArray[index++] = (byte) ((registerValues.get(i) >> 8) & 0xFF);  // High byte
//             }

//             // Debugging: Output the raw byte array and the extracted machine name
//             System.out.print("Raw bytes for machine name: ");
//             for (byte b : byteArray) {
//                 System.out.print(String.format("%02X ", b));  // Print each byte in hexadecimal format
//             }
//             System.out.println();

//             // Convert the byte array to a string using UTF-8 encoding
//             String batchName = new String(byteArray, StandardCharsets.UTF_8).trim();
//             System.out.println("Machine Name from HMI: " + batchName);
        
//             ZoneId zone = ZoneId.systemDefault();
//             LocalDateTime now = LocalDateTime.now(zone);
//             String localTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//             ModbusRecord record = new ModbusRecord(localTime, "dattime", ""+localTime);
//             ModbusRecord record1 = new ModbusRecord(localTime, "batchName", batchName);
//             ModbusRecord record2 = new ModbusRecord(localTime, "setWeight", ""+registerValues.get(0));
//             ModbusRecord record3 = new ModbusRecord(localTime, "actualWeight", ""+registerValues.get(1));
          
//             System.out.println("act weight" + record);
//             System.out.println("act weight" + record1);
//             System.out.println("act weight" + record2);
//             System.out.println("act weight" + record3);
       
//             List<ModbusRecord> recodsList = new ArrayList<>();
//             recodsList.add(record);
//             recodsList.add(record1);
//             recodsList.add(record2);
//             recodsList.add(record3);
            
//             ByteString byteString = toByteString(recodsList);
       


//             // ✅ Write encoded record to file
//             try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
//                 writer.write(Base64.getEncoder().encodeToString(byteString.toByteArray()));
//                 writer.newLine();
//                 System.out.println("✅ Wrote records as ByteStrings to file line by line");
//             } catch (IOException e) {
//                 System.err.println("❌ Error writing to file: " + e.getMessage());
//                 e.printStackTrace();
//             }
            
//         } catch (Exception e) {
//             System.err.println("⚠️ Modbus error: " + e.getMessage());
//         } finally {
//             if (connection != null) {
//                 try {
//                     connection.close();
//                 } catch (Exception ignored) {}
//             }
//         }
        
//     }
//     // to convert the list of objects to byteString
//      public static ByteString toByteString(List<ModbusRecord> list) throws IOException {
//         ByteArrayOutputStream bos = new ByteArrayOutputStream();
//         try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
//             oos.writeObject(list);
//         }
//         return ByteString.copyFrom(bos.toByteArray());
//     }
// }
package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
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
import java.util.Properties;

@Service
public class ModbusService {

    private String plcIp;
    private int plcPort;
    private final String filePath = "modbus-buffer.txt";
    private final String configFilePath = "modbus-config.txt";  // Path to the config file

    public ModbusService() {
        loadConfig();  // Load the configuration from file
    }

    // Load the IP address and port from the configuration file
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            Properties properties = new Properties();
            properties.load(fis);

            // Read the IP and Port values from the config file
            plcIp = properties.getProperty("ip", "192.168.1.2");  // Default value if not found
            plcPort = Integer.parseInt(properties.getProperty("port", "502"));  // Default port if not found

            System.out.println("Loaded Config - IP: " + plcIp + ", Port: " + plcPort);

        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void readModbusRegisters() {
        TCPMasterConnection connection = null;

        try {
            // Try to connect using the default IP address first
            connection = createConnection(plcIp);
            connection.connect();

            if (!connection.isConnected()) {
                // If the connection fails with the default IP, load the IP from the configuration file
                System.err.println("❌ Failed to connect using default IP, trying config file IP...");
                loadConfig();  // Reload config file to get the correct IP
                connection = createConnection(plcIp);
                connection.connect();
            }

            // If still not connected, throw an exception
            if (!connection.isConnected()) {
                throw new IOException("Unable to connect to PLC with IP: " + plcIp);
            }

            System.out.println("Connected successfully to PLC at IP: " + plcIp);

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

            // Extract machine name from registers 2 to 10 (9 registers = 18 bytes)
            byte[] byteArray = new byte[16]; // 9 registers, each 2 bytes
            int index = 0;
            for (int i = 2; i <= 9; i++) {
                byteArray[index++] = (byte) (registerValues.get(i) & 0xFF);         // Low byte
                byteArray[index++] = (byte) ((registerValues.get(i) >> 8) & 0xFF);  // High byte
            }

            String batchName = new String(byteArray, StandardCharsets.UTF_8).trim();
            System.out.println("Machine Name from HMI: " + batchName);

            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime now = LocalDateTime.now(zone);
            String localTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ModbusRecord record = new ModbusRecord(localTime, "dattime", "" + localTime);
            ModbusRecord record1 = new ModbusRecord(localTime, "batchName", batchName);
            ModbusRecord record2 = new ModbusRecord(localTime, "setWeight", "" + registerValues.get(0));
            ModbusRecord record3 = new ModbusRecord(localTime, "actualWeight", "" + registerValues.get(1));

            List<ModbusRecord> recodsList = new ArrayList<>();
            recodsList.add(record);
            recodsList.add(record1);
            recodsList.add(record2);
            recodsList.add(record3);

            ByteString byteString = toByteString(recodsList);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(Base64.getEncoder().encodeToString(byteString.toByteArray()));
                writer.newLine();
                System.out.println("✅ Wrote records as ByteStrings to file line by line");
            } catch (IOException e) {
                System.err.println("❌ Error writing to file: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Modbus error: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
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
}
