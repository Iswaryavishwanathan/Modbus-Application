package com.example.modbusapplication.Model;

public class ModbusData {
    private int[] values;
    private String timestamp;

    public ModbusData() {}

    public ModbusData(int[] values, String timestamp) {
        this.values = values;
        this.timestamp = timestamp;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
