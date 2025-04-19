package com.example.modbusapplication.Model;

import java.util.List;

public class ModbusRecord {
    private long timestamp;
    private String name;
    private List<Integer> registerValues;

    public ModbusRecord(long timestamp, String name, List<Integer> registerValues) {
        this.timestamp = timestamp;
        this.name = name;
        this.registerValues = registerValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getRegisterValues() {
        return registerValues;
    }
}


