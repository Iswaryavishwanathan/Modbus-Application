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
            while (true) {
                try {
                    modbusService.readModbusRegisters();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    System.err.println("Modbus Thread Error: " + e.getMessage());
                }
            }
        }, "Modbus-Thread").start();

        new Thread(() -> {
            while (true) {
                try {
                    uploaderService.uploadData();
                    Thread.sleep(5000);
                } catch (Exception e) {
                    System.err.println("Uploader Thread Error: " + e.getMessage());
                }
            }
        }, "Uploader-Thread").start();
    }
}
