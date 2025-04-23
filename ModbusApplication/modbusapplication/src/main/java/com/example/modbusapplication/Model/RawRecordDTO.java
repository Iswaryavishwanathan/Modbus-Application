package com.example.modbusapplication.Model;

public class RawRecordDTO {
    private String base64;

    public RawRecordDTO(String base64) {
        this.base64 = base64;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}

