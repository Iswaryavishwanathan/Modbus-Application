package com.example.modbusapplication.Config;

import com.example.modbusapplication.Service.ModbusService;
import com.example.modbusapplication.Service.UploaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThreadStarter {

   

    @Autowired
    private UploaderService uploaderService;
@Autowired
private ModbusService modbusService;

    private Thread modbusThread;
    private Thread uploadThread;

    @PostConstruct
    public void startThreads() {

if (modbusThread == null || !modbusThread.isAlive()) {
    modbusThread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                modbusService.conditionalWriteAtEveryMinute();  
                Thread.sleep(500);  
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

