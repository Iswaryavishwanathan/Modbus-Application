package com.example.modbusapplication.Service;

import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class ModbusService {

    private final String plcIp = "127.0.0.1"; // Change to your PLC IP
    private final int plcPort = 502;
    private int registerValue = 0;

    /**
     * Periodically reads Modbus Holding Register (Every 5 Seconds)
     */
    @Scheduled(fixedRate = 5000)
    public void readModbusRegister() {
        TCPMasterConnection connection = null;
        ModbusTCPTransaction transaction = null;

        try {
            // Establish Modbus TCP Connection
            InetAddress address = InetAddress.getByName(plcIp);
            connection = new TCPMasterConnection(address);
            connection.setPort(plcPort);
            connection.connect();

            // Create Request to Read Holding Register
            int registerAddress = 0;
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, 1);

            // Execute Transaction
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            // Get Response
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            Register register = response.getRegister(0);
            registerValue = register.getValue();

            System.out.println("Register 400001 Value: " + registerValue);

        } catch (Exception e) {
            System.err.println("Error reading Modbus Register: " + e.getMessage());
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
     * Get Latest Register Value
     */
    public int getLatestRegisterValue() {
        return registerValue;
    }
}
