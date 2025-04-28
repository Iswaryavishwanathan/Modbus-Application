package com.example.modbusapplication.Contoller;

import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Config.ThreadStarter;
import com.example.modbusapplication.Model.SystemConfig;
import com.example.modbusapplication.Service.SystemConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api")
public class Controlller {

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    ThreadStarter threadStarter;

    @GetMapping("/")
    public String getMethodName() {
        return new String("hiii");
    }
    @GetMapping("/plc-ip-port")
    public SystemConfig systemConfig() {
        return systemConfigService.readFromFile();
    }
    
    @PostMapping("/plc-ip-port")
    public ResponseEntity<?> postMethodName(@RequestBody SystemConfig systemConfig) {
        systemConfigService.saveToFiles(systemConfig);
            return  null;
    }
    
    @PostMapping("/start-modbus")
    public String startModBus() {
        threadStarter.startModbusThread();
        
        return "OK";
    }

    @PostMapping("/stop-modbus")
    public String stopModBus() {
        threadStarter.stopModbusThread();
        
        return "OK";
    }
    
    
    @PostMapping("/system-config")
    public ResponseEntity<String> updateSystemConfig(@RequestBody SystemConfig systemConfig) {
        boolean saved = systemConfigService.saveToFiles(systemConfig);
        boolean wifiUpdated = systemConfigService.updateWifiNetplan(systemConfig);
        
        if (saved && wifiUpdated) {
            return ResponseEntity.ok("✅ PLC, Raspi IP, and Wi-Fi Configurations Updated Successfully!");
        } else {
            return ResponseEntity.status(500).body("❌ Failed to update configurations.");
        }
    }

}
