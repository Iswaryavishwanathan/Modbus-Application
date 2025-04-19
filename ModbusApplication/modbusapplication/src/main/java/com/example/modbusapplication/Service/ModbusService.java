
package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ModbusService {

    private final String plcIp = "192.168.1.1";
    private final int plcPort = 502;
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

            List<Integer> registerValues = new ArrayList<>();
            for (int i = 0; i < response.getWordCount(); i++) {
                registerValues.add(response.getRegister(i).getValue());
            }

            // Extract the bytes from registers 3 to 10 (for a total of 8 registers)
            byte[] byteArray = new byte[16]; // 8 registers * 2 bytes per register = 16 bytes
            int index = 0;

            for (int i = 3; i < 10; i++) {
                byteArray[index++] = (byte) (registerValues.get(i) & 0xFF); // Low byte
                byteArray[index++] = (byte) ((registerValues.get(i) >> 8) & 0xFF); // High byte
            }

            // Convert bytes to a string
            String machineName = new String(byteArray, StandardCharsets.UTF_8).trim();

            System.out.println("Machine Name from HMI: " + machineName);

            // Print register values for debugging
            System.out.println("Integer Values:");
            for (int i = 0; i < registerValues.size(); i++) {
                System.out.println("  Register[" + i + "] = " + registerValues.get(i));
            }

            // Create and serialize one record
            ModbusRecord record = new ModbusRecord(System.currentTimeMillis(), machineName, registerValues);

            // Write the record to file in Base64
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                // 1. Write timestamp
                dos.writeLong(record.getTimestamp());

                // 2. Write name (length + bytes)
                byte[] nameBytes = record.getName().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(nameBytes.length);
                dos.write(nameBytes);

                // 3. Write each register value as a short
                for (int val : record.getRegisterValues()) {
                    dos.writeShort(val & 0xFFFF);
                }

                // Encode to Base64 and write to file
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                writer.write(base64);
                writer.newLine(); // New line after each record

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
}
