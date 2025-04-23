package com.example.modbusapplication.Model;

public class ModbusRecord {
    private String timestamp;
    private String name;
    private Registers registers;

    public ModbusRecord(String timestamp, String name, Registers registers) {
        this.timestamp = timestamp;
        this.name = name;
        this.registers = registers;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public Registers getRegisters() {
        return registers;
    }
}
