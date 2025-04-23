package com.example.modbusapplication.Config;

import com.example.modbusapplication.Service.ModbusService;
import com.example.modbusapplication.Service.UploaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThreadStarter {

    @Autowired
    private ModbusService modbusService;

    @Autowired
    private UploaderService uploaderService;

    @PostConstruct
    public void startThreads() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    modbusService.readModbusRegisters();
                    Thread.sleep(10000);  // Change or remove in production
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Modbus thread interrupted, stopping...");
                    break;
                } catch (Exception e) {
                    System.err.println("Modbus Thread Error: " + e.getMessage());
                }
            }
        }, "Modbus-Thread").start();
    
        new Thread(() -> {
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
        }, "Uploader-Thread").start();
    }
}    
