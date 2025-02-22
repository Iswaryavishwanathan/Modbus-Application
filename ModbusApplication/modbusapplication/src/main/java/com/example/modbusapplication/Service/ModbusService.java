package com.example.modbusapplication.Service;

import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Arrays;

@Service
public class ModbusService {

    private final String plcIp = "127.0.0.1"; // Change to your PLC IP
    private final int plcPort = 502;
    private int[] registerValues; // Store multiple register values
   
    /**
     * Periodically reads multiple Modbus Holding Registers (Every 5 Seconds)
     */
    @Scheduled(fixedRate = 3600000)
    public void readModbusRegisters() {
        TCPMasterConnection connection = null;
        ModbusTCPTransaction transaction = null;

        try {
            // Establish Modbus TCP Connection
            InetAddress address = InetAddress.getByName(plcIp);
            connection = new TCPMasterConnection(address);
            connection.setPort(plcPort);
            connection.connect();

            // Define Start Address and Number of Registers to Read
            int registerAddress = 0; // Start from register 400001 (0-based index)
            int quantity = 10; // Number of registers to read// Reads 400001 to 400010

            // Create Request to Read Multiple Holding Registers
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, quantity);
            

            // Execute Transaction
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();
    //        // Get Response
    // ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();

    // // Extract specific registers (e.g., 400001, 400005, 400010)
    // int value1 = response.getRegister(0).getValue(); // 400001
    // int value2 = response.getRegister(4).getValue(); // 400005
    // int value3 = response.getRegister(9).getValue(); // 400010

    // // Print specific values
    // System.out.println("Register 400001: " + value1);
    // System.out.println("Register 400005: " + value2);
    // System.out.println("Register 400010: " + value3);



            // Get Response
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            registerValues = new int[response.getWordCount()];

            // Extract and Store All Register Values
            for (int i = 0; i < response.getWordCount(); i++) {
                registerValues[i] = response.getRegister(i).getValue();
            }

            System.out.println("Register Values: " + Arrays.toString(registerValues));

        } catch (Exception e) {
            System.err.println("Error reading Modbus Registers: " + e.getMessage());
        } finally {
            // Close Connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Get Latest Register Values
     */
    public int[] getLatestRegisterValues() {
        return registerValues;
    }
}
