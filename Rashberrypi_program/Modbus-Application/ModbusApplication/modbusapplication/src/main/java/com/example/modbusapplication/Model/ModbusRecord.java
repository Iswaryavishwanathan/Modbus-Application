package com.example.modbusapplication.Model;

import java.io.Serializable;

public class ModbusRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public ModbusRecord(String name, String value) {

        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    @Override
public String toString() {
    return "ModbusRecord{" +
            ", name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
}

}
