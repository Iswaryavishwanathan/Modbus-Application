package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.Registers;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ModbusService {

    private final String plcIp = "192.168.1.1";
    private final int plcPort = 502;
    private final String filePath = "modbus-buffer.txt";

    // @Scheduled(fixedRate = 10000)
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

            List<Integer> registerValues = new ArrayList<>();
            for (int i = 0; i < response.getWordCount(); i++) {
                registerValues.add(response.getRegister(i).getValue());
            }

            // ✅ Extract machine name from registers 2 to 10 (9 registers = 18 bytes)
            byte[] byteArray = new byte[16]; // 9 registers, each 2 bytes
            int index = 0;

            // Loop through registers 2 to 10 and get their low and high bytes
            for (int i = 2; i <=9 ; i++) {
                byteArray[index++] = (byte) (registerValues.get(i) & 0xFF);         // Low byte
                byteArray[index++] = (byte) ((registerValues.get(i) >> 8) & 0xFF);  // High byte
            }

            // Debugging: Output the raw byte array and the extracted machine name
            System.out.print("Raw bytes for machine name: ");
            for (byte b : byteArray) {
                System.out.print(String.format("%02X ", b));  // Print each byte in hexadecimal format
            }
            System.out.println();

            // Convert the byte array to a string using UTF-8 encoding
            String machineName = new String(byteArray, StandardCharsets.UTF_8).trim();
            System.out.println("Machine Name from HMI: " + machineName);



            // Debugging: Show register values
            System.out.println("🔢 Integer Register Values:");
            for (int i = 0; i < registerValues.size(); i++) {
                System.out.println("  Register[" + i + "] = " + registerValues.get(i));
            }

            Registers registers = new Registers(
                    registerValues.get(0), registerValues.get(1), registerValues.get(2),
                    registerValues.get(3), registerValues.get(4), registerValues.get(5),
                    registerValues.get(6), registerValues.get(7), registerValues.get(8),
                    registerValues.get(9)
            );

            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime now = LocalDateTime.now(zone);
            String localTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ModbusRecord record = new ModbusRecord(localTime, machineName, registers);

            // ✅ Logging
            System.out.println("\n--- Data to be written ---");
            System.out.println("Timestamp: " + record.getTimestamp());
            System.out.println("Machine Name: " + record.getName());
            System.out.println("Register Values:");
            System.out.print(record.getRegisters().getReg0() + " ");
            System.out.print(record.getRegisters().getReg1() + " ");
            System.out.print(record.getRegisters().getReg2() + " ");
            System.out.print(record.getRegisters().getReg3() + " ");
            System.out.print(record.getRegisters().getReg4() + " ");
            System.out.print(record.getRegisters().getReg5() + " ");
            System.out.print(record.getRegisters().getReg6() + " ");
            System.out.print(record.getRegisters().getReg7() + " ");
            System.out.print(record.getRegisters().getReg8() + " ");
            System.out.print(record.getRegisters().getReg9() + " ");
            System.out.println("\n--------------------------");

            // ✅ Write encoded record to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                byte[] timeBytes = record.getTimestamp().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(timeBytes.length);
                dos.write(timeBytes);


                byte[] nameBytes = record.getName().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(nameBytes.length);
                dos.write(nameBytes);

                dos.writeShort(record.getRegisters().getReg0());
                dos.writeShort(record.getRegisters().getReg1());
                dos.writeShort(record.getRegisters().getReg2());
                dos.writeShort(record.getRegisters().getReg3());
                dos.writeShort(record.getRegisters().getReg4());
                dos.writeShort(record.getRegisters().getReg5());
                dos.writeShort(record.getRegisters().getReg6());
                dos.writeShort(record.getRegisters().getReg7());
                dos.writeShort(record.getRegisters().getReg8());
                dos.writeShort(record.getRegisters().getReg9());

                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                writer.write(base64);
                writer.newLine();

                System.out.println("✅ Wrote 1 record to file");
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
    public int readActualWeightOnly() {
        TCPMasterConnection connection = null;
        try {
            InetAddress address = InetAddress.getByName(plcIp);
            connection = new TCPMasterConnection(address);
            connection.setPort(plcPort);
            connection.connect();
    
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(1, 1); // reg 1 = actual weight
            request.setUnitID(1);
    
            ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();
    
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            return response.getRegister(0).getValue();
    
        } catch (Exception e) {
            System.err.println("⚠️ Error reading actual weight: " + e.getMessage());
            return -1;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }
    
        
}
