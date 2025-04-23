package com.example.modbusapplication.Model;

import java.io.Serializable;

public class ModbusRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String timestamp;
    private String name;
    private String value;

    public ModbusRecord(String timestamp, String name, String value) {
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getRegisters() {
        return value;
    }
    @Override
public String toString() {
    return "ModbusRecord{" +
            "timestamp='" + timestamp + '\'' +
            ", name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
}

}
