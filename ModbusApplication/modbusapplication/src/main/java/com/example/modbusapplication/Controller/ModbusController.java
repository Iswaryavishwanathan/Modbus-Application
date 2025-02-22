package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.modbusapplication.Service.ModbusService;

@RestController
@RequestMapping("/modbus")
public class ModbusController {
     private final ModbusService modbusService;

    public ModbusController(ModbusService modbusService) {
        this.modbusService = modbusService;
    }
    @GetMapping("/values")
    public int[] getRegisterValues() {
        return modbusService.getLatestRegisterValues();
    }

}
