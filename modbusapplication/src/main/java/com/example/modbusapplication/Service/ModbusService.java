package com.example.modbusapplication.Service;

// import com.example.modbusapplication.Model.ModbusData;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
// import java.io.FileWriter;
import java.net.InetAddress;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.Arrays;

@Service
public class ModbusService {

    private final String plcIp = "192.168.1.1";
    private final int plcPort = 502;
    // private final ObjectMapper mapper = new ObjectMapper();
    private final String filePath = "modbus-buffer.txt";

    @Scheduled(fixedRate = 1000)
    public void readModbusRegisters() {
        TCPMasterConnection connection = null;

        try {
            InetAddress address = InetAddress.getByName(plcIp);
            connection = new TCPMasterConnection(address);
            connection.setPort(plcPort);
            connection.connect();

            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(0, 10);
            request.setUnitID(1);

            ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            int[] registerValues = new int[response.getWordCount()];
            for (int i = 0; i < response.getWordCount(); i++) {
                registerValues[i] = response.getRegister(i).getValue();
            }

             // Save to binary file as bytes
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath, true))) {
                // Write current timestamp as 8-byte long
                dos.writeLong(System.currentTimeMillis());
                   // Write register values as 2-byte shorts
                for (int value : registerValues) {
                    dos.writeShort(value); // 2 bytes per register (16-bit)
                }
            }

            System.out.println("Written " + registerValues.length + " registers to binary file.");


            // System.out.println("Register Values: " + Arrays.toString(registerValues));

        //     // Save to file
        //    // Use formatted timestamp
        //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //     String formattedTimestamp = LocalDateTime.now().format(formatter);

        //     ModbusData data = new ModbusData(registerValues, formattedTimestamp);
        //     String json = mapper.writeValueAsString(data);

        //     try (FileWriter writer = new FileWriter(filePath, true)) {
        //         writer.write(json + "\n");
        //     }

        } catch (Exception e) {
            System.err.println("Error reading Modbus Registers: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }
}

// package com.example.modbusapplication.Service;

// import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
// import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
// import org.springframework.stereotype.Service;

// import java.net.InetAddress;
// import java.util.*;

// @Service
// public class ModbusService {

//     private final String plcIp = "192.168.1.1"; // Change this to your PLC IP
//     private final int plcPort = 502;

//     /**
//      * 🔹 Reads only the registers sent from the frontend and formats the response.
//      */
//     public List<Map<String, Object>> readRegisters(List<Map<String, Object>> registerDatas) {
//         List<Map<String, Object>> registerValues = new ArrayList<>();
//         TCPMasterConnection connection = null;

//         try {
//             // Establish Modbus TCP Connection
//             InetAddress address = InetAddress.getByName(plcIp);
//             connection = new TCPMasterConnection(address);
//             connection.setPort(plcPort);
//             connection.connect();
//             // 🔹 Print PLC connection status
// if (connection.isConnected()) {
//     System.out.println("✅ Connected to PLC at " + plcIp + ":" + plcPort);
// } else {
//     System.out.println("❌ Failed to connect to PLC at " + plcIp + ":" + plcPort);
// }

//             for (Map<String, Object> reg : registerDatas) {
//                 int registerAddress = (int) reg.get("register");
//                 int length = (int) reg.get("length");
//                 String name = (String) reg.get("name");

//                 // Read Modbus register
//                 ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, length);
//                 request.setUnitID(1);

//                 ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//                 transaction.setRequest(request);
//                 transaction.execute();

//                 ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
//                 List<Integer> values = new ArrayList<>();
//                 for (int i = 0; i < length; i++) {
//                     values.add(response.getRegister(i).getValue());
//                 }
//                 // 🔹 Print the read values for debugging
//     System.out.println("Register Name: " + name);
//     System.out.println("Register Address: " + registerAddress);
//     System.out.println("Length: " + length);
//     System.out.println("Values: " + values);

//                 // Format response as per UI requirements
//                 Map<String, Object> valueMap = new HashMap<>();
//                 valueMap.put("name", name);
//                 valueMap.put("value", values);

//                 registerValues.add(valueMap);
//             }

//         } catch (Exception e) {
//             System.err.println("Error reading Modbus Registers: " + e.getMessage());
//         } finally {
//             if (connection != null) {
//                 try {
//                     connection.close();
//                 } catch (Exception ignored) {
//                 }
//             }
//         }
//         return registerValues;
//     }
// }

// package com.example.modbusapplication.Service;

// import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
// import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.net.InetAddress;
// import java.util.*;

// @Service
// public class ModbusService {

//     private final String plcIp = "192.168.1.1"; // Change this to your PLC IP
//     private final int plcPort = 502;
    
//     private Map<Integer, Integer> latestScheduledValues = new HashMap<>(); // Store live scheduled values
//     private List<Integer> scheduledRegisters = Arrays.asList(1, 2, 3, 4, 5,6 ,7 ,8 ,9, 10, 11); // Default registers for scheduler

//     /**
//      * 🔹 Scheduled reading (Every 5 Seconds)
//      */
//     @Scheduled(fixedRate = 10000) // Runs every 10 seconds
//     public void scheduledReadModbusRegisters() {
//         latestScheduledValues = readRegisters(scheduledRegisters);
//         System.out.println("Scheduled Read: " + latestScheduledValues);
//     }

//     /**
//      * 🔹 Read specific registers (Used by scheduler & frontend)
//      */
//     public Map<Integer, Integer> readRegisters(List<Integer> registers) {
//         Map<Integer, Integer> registerValues = new HashMap<>();
//         TCPMasterConnection connection = null;

//         try {
//             // Establish Modbus TCP Connection
//             InetAddress address = InetAddress.getByName(plcIp);
//             connection = new TCPMasterConnection(address);
//             connection.setPort(plcPort);
//             connection.connect();

//             // Read each register individually
//             for (int registerAddress : registers) {
//                 ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, 1);
//                 request.setUnitID(1); 

//                 ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//                 transaction.setRequest(request);
//                 transaction.execute();

//                 // Get response
//                 ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
//                 int value = response.getRegister(0).getValue();

//                 // Store the register value in the map
//                 registerValues.put(registerAddress, value);
//             }

//         } catch (Exception e) {
//             System.err.println("Error reading Modbus Registers: " + e.getMessage());
//         } finally {
//             if (connection != null) {
//                 try {
//                     connection.close();
//                 } catch (Exception ignored) {
//                 }
//             }
//         }
//         return registerValues;
//     }

//     /**
//      * 🔹 Get the latest scheduled register values
//      */
//     public Map<Integer, Integer> getLatestScheduledValues() {
//         return latestScheduledValues;
//     }
// }
// package com.example.modbusapplication.Service;

// import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
// import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
// import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
// import org.springframework.stereotype.Service;

// import java.net.InetAddress;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @Service
// public class ModbusService {

//     private final String plcIp = "192.168.1.1"; // Change this to your PLC IP
//     private final int plcPort = 502;

//     /**
//      * 🔹 Reads only the registers sent from the frontend
//      */
//     public Map<Integer, Integer> readRegisters(List<Integer> registers) {
//         Map<Integer, Integer> registerValues = new HashMap<>();
//         TCPMasterConnection connection = null;

//         try {
//             // Establish Modbus TCP Connection
//             InetAddress address = InetAddress.getByName(plcIp);
//             connection = new TCPMasterConnection(address);
//             connection.setPort(plcPort);
//             connection.connect();

//             // Read each register individually
//             for (int registerAddress : registers) {
//                 ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, 1);
//                 request.setUnitID(1); 

//                 ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//                 transaction.setRequest(request);
//                 transaction.execute();

//                 // Get response
//                 ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
//                 int value = response.getRegister(0).getValue();

//                 // Store the register value in the map
//                 registerValues.put(registerAddress, value);
//             }

//         } catch (Exception e) {
//             System.err.println("Error reading Modbus Registers: " + e.getMessage());
//         } finally {
//             if (connection != null) {
//                 try {
//                     connection.close();
//                 } catch (Exception ignored) {
//                 }
//             }
//         }
//         return registerValues;
//     }
// }
