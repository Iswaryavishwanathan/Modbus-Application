package com.example.modbusapplication.Config;

import com.example.modbusapplication.Service.ModbusService;
import com.example.modbusapplication.Service.UploaderService;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThreadStarter {

   

    @Autowired
    private UploaderService uploaderService;

    private Thread modbusThread;
    private Thread uploadThread;

    @PostConstruct
    public void startThreads() {
       if (modbusThread == null || !modbusThread.isAlive()) {
    ModbusService modbusService = new ModbusService();
    modbusThread = new Thread(() -> {
        LocalDateTime lastWrittenTime = LocalDateTime.now().minusMinutes(1);  // Force first write
        int lastTotalWeight = 0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int totalWeight = modbusService.peekTotalWeightRegister8(); // Only register 8
                LocalDateTime now = LocalDateTime.now();

                boolean isTimeToWrite = lastWrittenTime.plusMinutes(1).isBefore(now);
                boolean isWeightJustNowNonZero = (totalWeight > 0 && lastTotalWeight == 0);

                if (isTimeToWrite || isWeightJustNowNonZero) {
                    modbusService.readModbusRegisters();  // Write full data to file
                    lastWrittenTime = now;
                    System.out.println("✅ Written to file at " + now + ", Total Weight = " + totalWeight);
                }

                lastTotalWeight = totalWeight; // Update for next comparison
                Thread.sleep(500);  // Check every 0.5 sec

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Modbus thread interrupted, stopping...");
                break;
            } catch (Exception e) {
                System.err.println("Modbus Thread Error: " + e.getMessage());
            }
        }
    }, "Modbus-Thread");
    modbusThread.start();
}

        if (uploadThread == null || !uploadThread.isAlive()) {
            uploadThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        uploaderService.uploadData();
                        Thread.sleep(5000);  // Change or remove in production
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Uploader thread interrupted, stopping...");
                        break;
                    } catch (Exception e) {
                        System.err.println("Uploader Thread Error: " + e.getMessage());
                    }
                }
            }, "Uploader-Thread");
            uploadThread.start();
        }
    }
  

    public synchronized void stopModbusThread() {
        if (modbusThread != null && modbusThread.isAlive()) {
            modbusThread.interrupt();
        }
    }

    public synchronized void stopUploadThread() {
        if (uploadThread != null && uploadThread.isAlive()) {
            uploadThread.interrupt();
        }
    }

    public synchronized void startModbusThread() {
        if (modbusThread == null || !modbusThread.isAlive()) {
            modbusThread = new Thread(() -> {
                ModbusService modbusService = new ModbusService();
                boolean flag = true;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (flag) {
                            modbusService.increament();
                            flag = false;
                        }

                        modbusService.readModbusRegisters();
                        Thread.sleep(1000);  // Change or remove in production
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Modbus thread interrupted, stopping...");
                        break;
                    } catch (Exception e) {
                        System.err.println("Modbus Thread Error: " + e.getMessage());
                    }
                }
            }, "Modbus-Thread");
            modbusThread.start();
        }
    }
    
    

    public synchronized void startUploadThread() {
        if (uploadThread == null || !uploadThread.isAlive()) {
            uploadThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        uploaderService.uploadData();
                        Thread.sleep(5000);  // Change or remove in production
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Uploader thread interrupted, stopping...");
                        break;
                    } catch (Exception e) {
                        System.err.println("Uploader Thread Error: " + e.getMessage());
                    }
                }
            }, "Uploader-Thread");
            uploadThread.start();
        }
    }
}

