package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.SystemConfig;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ModbusService {

    private String plcIp;
    private int plcPort;
    private String deviceId;
    private final String filePath = "modbus-buffer.txt";
    int m = 10;
    private LocalDateTime lastWrittenTime = LocalDateTime.now().minusMinutes(1);
    private int lastTotalWeight = -1;
    private boolean ledOn = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> ledOffTask = null;

    public ModbusService() {
        loadConfig();
        turnLedOff();
    }

    private void loadConfig() {
        System.out.println("entered loadConfig");
        try {
            SystemConfigService systemConfigService = new SystemConfigService();
            SystemConfig systemConfig = systemConfigService.readFromFile();
            plcIp = systemConfig.getIpAddress_plc();
            plcPort = Integer.parseInt(systemConfig.getPort_plc());
            System.out.println("Loaded Config - IP: " + plcIp + ", Port: " + plcPort);
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
            turnLedOff();
        }
    }

    private void turnLedOn() {
        if (!ledOn) {
            try {
                System.out.println("Attempting to turn LED ON");
                Process process = Runtime.getRuntime().exec(new String[]{"/home/pi/control_led.sh", "1"});
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("LED turned ON successfully");
                    ledOn = true;
                } else {
                    System.err.println("Failed to turn ON LED: control_led.sh exited with code " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to turn ON LED: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("LED already ON, skipping command");
        }
    }

    private void turnLedOff() {
        if (ledOn) {
            try {
                System.out.println("Attempting to turn LED OFF");
                Process process = Runtime.getRuntime().exec(new String[]{"/home/pi/control_led.sh", "0"});
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("LED turned OFF successfully");
                    ledOn = false;
                } else {
                    System.err.println("Failed to turn OFF LED: control_led.sh exited with code " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to turn OFF LED: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("LED already OFF, skipping command");
        }
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        System.out.println("Application shutting down");
        if (ledOffTask != null) {
            ledOffTask.cancel(false);
        }
        turnLedOff();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Application shutdown complete, LED turned OFF");
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
                    loadConfig();
                    connection = createConnection(plcIp);
                    connection.connect();
                }

                if (!connection.isConnected()) {
                    throw new IOException("Unable to connect to PLC at IP: " + plcIp);
                }

                System.out.println("Connected to PLC at IP: " + plcIp);

                int startOffset = 400;
                int count = 25;

                ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(startOffset, count);
                request.setUnitID(1);

                ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
                transaction.setRequest(request);
                transaction.execute();

                ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();

                List<Integer> registerValues = new ArrayList<>();
                for (int i = 0; i < response.getWordCount(); i++) {
                    registerValues.add(response.getRegister(i).getValue());
                }

                if (registerValues.isEmpty()) {
                    throw new IOException("No values read from PLC");
                }

                // // Extract and add registers 40408 and 40409
                //  int scaleFactor = 100;
                // int register408Value = registerValues.get(8)/ scaleFactor; // Register 40408
                // int register409Value = registerValues.get(9)/ scaleFactor; // Register 40409
                // int totalWeight = register408Value + register409Value;

                // // Print only the total weight
                // System.out.println("Total Weight (408 + 409) = " + totalWeight);

                for (int i = 0; i < response.getWordCount(); i++) {
                    int value = registerValues.get(i);
                    int actualAddress = 40000 + startOffset + i;
                    System.out.println("Register[" + actualAddress + "] = " + value);
                }

                byte[] byteArray = new byte[12];
                int index = 0;
                for (int i = 16; i <= 21; i++) {
                    int value = registerValues.get(i);
                    byteArray[index++] = (byte) (value & 0xFF);
                    byteArray[index++] = (byte) ((value >> 8) & 0xFF);
                }
                String batchName = new String(byteArray, StandardCharsets.UTF_8).trim();
                System.out.println("Machine Name from HMI: " + batchName);

                deviceId = systemConfig.getDeviceId();
                System.out.println("Device ID: " + deviceId);

                ZoneId zone = ZoneId.systemDefault();
                LocalDateTime now = LocalDateTime.now(zone);
                String localTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

               // Scale factor (assumed ×100)
                int scaleFactor = 100;

                int setWeight = registerValues.get(2) / scaleFactor;
                int presentWeight = registerValues.get(12) / scaleFactor;
                // Assuming totalWeight is in registers 40408 (low) and 40409 (high)
                int low = registerValues.get(8);   // 40408
                int high = registerValues.get(9);  // 40409
                int fullTotalWeight = (high << 16) | (low & 0xFFFF);  // 32-bit value
                int totalWeight = fullTotalWeight / 100;
    
                List<ModbusRecord> recodsList = new ArrayList<>();
                recodsList.add(new ModbusRecord("datetime", localTime));
                recodsList.add(new ModbusRecord("batchName", batchName));
                recodsList.add(new ModbusRecord("setWeight", String.valueOf(setWeight)));
                recodsList.add(new ModbusRecord("presentWeight", String.valueOf(presentWeight)));
                recodsList.add(new ModbusRecord("totalWeight", String.valueOf(totalWeight)));
                recodsList.add(new ModbusRecord("deviceId", deviceId));
            System.out.println("datetime: " + localTime);
            System.out.println("lotname: " + batchName);
            System.out.println("setweight: " + setWeight);
            System.out.println("presentweight: " + presentWeight);
            System.out.println("totalweight: " + totalWeight);
            System.out.println("deviceid: " + deviceId);
     

                ByteString byteString = toByteString(recodsList);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    writer.write(Base64.getEncoder().encodeToString(byteString.toByteArray()));
                    writer.newLine();
                    System.out.println("Wrote records as ByteString to file.");
                    turnLedOn();
                    if (ledOffTask != null) {
                        ledOffTask.cancel(false);
                        System.out.println("Canceled previous LED OFF timer");
                    }
                    ledOffTask = scheduler.schedule(() -> {
                        turnLedOff();
                        System.out.println("Scheduled LED OFF after 1 minute");
                    }, 60, TimeUnit.SECONDS);
                    System.out.println("Scheduled new LED OFF timer for 1 minute");
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                    e.printStackTrace();
                    turnLedOff();
                    if (ledOffTask != null) {
                        ledOffTask.cancel(false);
                        System.out.println("Canceled LED OFF timer due to file write failure");
                    }
                }

                break;

            } catch (Exception e) {
                System.err.println("Modbus error: " + e.getMessage());
                turnLedOff();
                if (ledOffTask != null) {
                    ledOffTask.cancel(false);
                    System.out.println("Canceled LED OFF timer due to Modbus error");
                }

                try {
                    if (connection != null) connection.close();
                    System.out.println("Retrying in 2 seconds...");
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }

    public void increament() {
        System.out.println(m);
        m += 1;
    }

    private TCPMasterConnection createConnection(String ip) throws IOException {
        try {
            InetAddress address = InetAddress.getByName(ip);
            TCPMasterConnection connection = new TCPMasterConnection(address);
            connection.setPort(plcPort);
            return connection;
        } catch (Exception e) {
            System.err.println("Failed to create connection: " + e.getMessage());
            turnLedOff();
            if (ledOffTask != null) {
                ledOffTask.cancel(false);
                System.out.println("Canceled LED OFF timer due to connection failure");
            }
            throw new IOException("Failed to create connection: " + e.getMessage());
        }
    }

    public static ByteString toByteString(List<ModbusRecord> list) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(list);
        }
        return ByteString.copyFrom(bos.toByteArray());
    }

    public int peekTotalWeightRegister8() {
        try {
            int[] registers = readRegistersRaw();
            if (registers.length > 9) {
                return registers[8] + registers[9];
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Error reading total weight (registers 8 and 9): " + e.getMessage());
            turnLedOff();
            if (ledOffTask != null) {
                ledOffTask.cancel(false);
                System.out.println("Canceled LED OFF timer due to peek failure");
            }
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

            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(400, 25);
            request.setUnitID(1);

            ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            int[] registers = new int[response.getWordCount()];
            for (int i = 0; i < response.getWordCount(); i++) {
                registers[i] = response.getRegister(i).getValue();
            }

            if (registers.length == 0) {
                throw new IOException("No values read from PLC");
            }

            return registers;

        } catch (Exception e) {
            System.err.println("readRegistersRaw :: Modbus error :: " + e.getMessage());
            turnLedOff();
            if (ledOffTask != null) {
                ledOffTask.cancel(false);
                System.out.println("Canceled LED OFF timer due to readRegistersRaw error");
            }
            return new int[0];
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public void conditionalWriteAtEveryMinute() {
        try {
            int currentWeight = peekTotalWeightRegister8();
            LocalDateTime now = LocalDateTime.now();

            boolean isSecondZero = (now.getSecond() == 0);
            boolean isNewMinute = lastWrittenTime.getMinute() != now.getMinute();

            boolean isTimeToWrite = isSecondZero && isNewMinute;
            boolean hasWeightChanged = (currentWeight != -1 && currentWeight != lastTotalWeight);

            if (isTimeToWrite || hasWeightChanged) {
                readModbusRegisters();
                lastWrittenTime = now;
                lastTotalWeight = currentWeight;
                System.out.println("Written to file at " + now + ", Total Weight = " + currentWeight);
            }

        } catch (Exception e) {
            System.err.println("conditionalWriteAtEveryMinute error: " + e.getMessage());
            turnLedOff();
            if (ledOffTask != null) {
                ledOffTask.cancel(false);
                System.out.println("Canceled LED OFF timer due to conditionalWrite error");
            }
        }
    }
}
