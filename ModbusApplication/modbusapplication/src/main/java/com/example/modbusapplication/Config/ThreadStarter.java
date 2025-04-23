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

        // 🔁 1. Regular 1-hour data reader
        new Thread(() -> {
            while (true) {
                try {
                    modbusService.readModbusRegisters(); // full data
                    Thread.sleep(3600_000); // 1 hour = 3600000 ms
                } catch (Exception e) {
                    System.err.println("Hourly Reader Error: " + e.getMessage());
                }
            }
        }, "Hourly-Reader").start();

        // 🕵️‍♂️ 2. Monitor actual weight and trigger full read on 0
        new Thread(() -> {
            boolean hasReadAfterZero = false;

            while (true) {
                try {
                    int actualWeight = modbusService.readActualWeightOnly();
                    System.out.println("🧪 Actual Weight: " + actualWeight);

                    if (actualWeight == 0 && !hasReadAfterZero) {
                        System.out.println("📥 Weight = 0, triggering full data read...");
                        modbusService.readModbusRegisters();
                        hasReadAfterZero = true;
                    } else if (actualWeight > 0) {
                        hasReadAfterZero = false; // Reset flag
                    }

                    Thread.sleep(1000); // Check every second
                } catch (Exception e) {
                    System.err.println("Actual Weight Monitor Error: " + e.getMessage());
                }
            }
        }, "Weight-Monitor").start();

        // ☁️ 3. Uploader Thread
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
