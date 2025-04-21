package com.example.modbusapplication.Model;

public class ModbusRecord {
    private long timestamp;
    private String name;
    private Registers registers;

    public ModbusRecord(long timestamp, String name, Registers registers) {
        this.timestamp = timestamp;
        this.name = name;
        this.registers = registers;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public Registers getRegisters() {
        return registers;
    }
}
