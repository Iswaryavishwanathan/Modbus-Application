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
                boolean hasWrittenZeroWeight = false;  // Track if weight 0 was written already
                boolean hasWrittenAfterZero = false;  // Track if weight > 0 after 0 has been written
                boolean hasWrittenAtTopOfHour = false;  // Track if data has been written at the top of the hour

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int currentWeight = modbusService.peekActualWeight();
                        LocalDateTime now = LocalDateTime.now();

                        // Check if it's the top of the hour (minute 0, second 0)
                        boolean isHourPassed = now.getMinute() == 0 && now.getSecond() == 0;

                        // Check if actual weight is 0
                        boolean isWeightZero = currentWeight == 0;

                        // Write every hour or when weight becomes 0, but only once for each case
                        if (isHourPassed && !hasWrittenAtTopOfHour) {
                            modbusService.readModbusRegisters();  // Write to file at top of hour
                            hasWrittenAtTopOfHour = true;
                            System.out.println("✅ Written to file at " + now + " (Top of the Hour)");
                        }

                        if (isWeightZero && !hasWrittenZeroWeight) {
                            modbusService.readModbusRegisters();  // Write to file when weight is 0
                            hasWrittenZeroWeight = true;
                            hasWrittenAfterZero = false;  // Reset when weight is 0
                            System.out.println("✅ Written to file when weight became 0 at " + now);
                        }

                        // Write once when actual weight increases from 0 to non-zero
                        if (currentWeight > 0 && hasWrittenZeroWeight && !hasWrittenAfterZero) {
                            modbusService.readModbusRegisters();  // Write to file when weight > 0 after 0
                            hasWrittenAfterZero = true;
                            System.out.println("⬆️ Written to file when weight increased from 0 to " + currentWeight);
                        }

                        // Reset written flags after the hour or weight 0 event is processed
                        if (!isHourPassed && hasWrittenAtTopOfHour) {
                            hasWrittenAtTopOfHour = false;
                        }

                        if (!isWeightZero && hasWrittenZeroWeight) {
                            hasWrittenZeroWeight = false;
                        }

                        Thread.sleep(500);  // Check every second

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

// package com.example.modbusapplication.Config;

// import com.example.modbusapplication.Service.ModbusService;
// import com.example.modbusapplication.Service.UploaderService;
// import jakarta.annotation.PostConstruct;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// @Component
// public class ThreadStarter {

   

//     @Autowired
//     private UploaderService uploaderService;

//     private Thread modbusThread;
//     private Thread uploadThread;

//     @PostConstruct
//     public void startThreads() {
//         if (modbusThread == null || !modbusThread.isAlive()) {
//             ModbusService modbusService = new ModbusService();
//             modbusThread = new Thread(() -> {
//                 boolean flag = true;
//                 while (!Thread.currentThread().isInterrupted()) {
//                     try {
//                         if (flag) {
//                             modbusService.increament();
//                             flag = false;
//                         }

//                         modbusService.readModbusRegisters();
//                         Thread.sleep(1000);  // Change or remove in production
//                     } catch (InterruptedException e) {
//                         Thread.currentThread().interrupt();
//                         System.out.println("Modbus thread interrupted, stopping...");
//                         break;
//                     } catch (Exception e) {
//                         System.err.println("Modbus Thread Error: " + e.getMessage());
//                     }
//                 }
//             }, "Modbus-Thread");
//             modbusThread.start();
//         }

//         if (uploadThread == null || !uploadThread.isAlive()) {
//             uploadThread = new Thread(() -> {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     try {
//                         uploaderService.uploadData();
//                         Thread.sleep(5000);  // Change or remove in production
//                     } catch (InterruptedException e) {
//                         Thread.currentThread().interrupt();
//                         System.out.println("Uploader thread interrupted, stopping...");
//                         break;
//                     } catch (Exception e) {
//                         System.err.println("Uploader Thread Error: " + e.getMessage());
//                     }
//                 }
//             }, "Uploader-Thread");
//            // uploadThread.start();
//         }
//     }

//     public synchronized void stopModbusThread() {
//         if (modbusThread != null && modbusThread.isAlive()) {
//             modbusThread.interrupt();
//         }
//     }

//     public synchronized void stopUploadThread() {
//         if (uploadThread != null && uploadThread.isAlive()) {
//             uploadThread.interrupt();
//         }
//     }

//     public synchronized void startModbusThread() {
//         if (modbusThread == null || !modbusThread.isAlive()) {
//             modbusThread = new Thread(() -> {
//                 ModbusService modbusService = new ModbusService();
//                 boolean flag = true;
//                 while (!Thread.currentThread().isInterrupted()) {
//                     try {
//                         if (flag) {
//                             modbusService.increament();
//                             flag = false;
//                         }

//                         modbusService.readModbusRegisters();
//                         Thread.sleep(500);  // Change or remove in production
//                     } catch (InterruptedException e) {
//                         Thread.currentThread().interrupt();
//                         System.out.println("Modbus thread interrupted, stopping...");
//                         break;
//                     } catch (Exception e) {
//                         System.err.println("Modbus Thread Error: " + e.getMessage());
//                     }
//                 }
//             }, "Modbus-Thread");
//             modbusThread.start();
//         }
//     }

//     public synchronized void startUploadThread() {
//         if (uploadThread == null || !uploadThread.isAlive()) {
//             uploadThread = new Thread(() -> {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     try {
//                         uploaderService.uploadData();
//                         Thread.sleep(5000);  // Change or remove in production
//                     } catch (InterruptedException e) {
//                         Thread.currentThread().interrupt();
//                         System.out.println("Uploader thread interrupted, stopping...");
//                         break;
//                     } catch (Exception e) {
//                         System.err.println("Uploader Thread Error: " + e.getMessage());
//                     }
//                 }
//             }, "Uploader-Thread");
//             uploadThread.start();
//         }
//     }
// }
