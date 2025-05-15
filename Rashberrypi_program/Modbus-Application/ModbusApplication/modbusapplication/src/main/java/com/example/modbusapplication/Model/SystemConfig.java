package com.example.modbusapplication.Model;

import java.io.Serializable;

public class SystemConfig implements Serializable {
    private String ipAddress_plc;
    private String port_plc;
    private String ipaddress_rashpi;
    private String wifiName;
    private String wifiPassword;
    private String deviceName; 
    public String getIpAddress_plc() {
        return ipAddress_plc;
    }
   
    public SystemConfig(String ipAddress_plc, String port_plc, String ipaddress_rashpi, String wifiName, String wifiPassword, String deviceName) {
        this.ipAddress_plc = ipAddress_plc;
        this.port_plc = port_plc;
        this.ipaddress_rashpi = ipaddress_rashpi;
        this.wifiName = wifiName;
        this.wifiPassword = wifiPassword;
        this.deviceName = deviceName;
    }
    public String getIpaddress_rashpi() {
        return ipaddress_rashpi;
    }
    public void setIpaddress_rashpi(String ipaddress_rashpi) {
        this.ipaddress_rashpi = ipaddress_rashpi;
    }
    public String getWifiName() {
        return wifiName;
    }
    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }
    public String getWifiPassword() {
        return wifiPassword;
    }
    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }
    public void setIpAddress_plc(String ipAddress_plc) {
        this.ipAddress_plc = ipAddress_plc;
    }
    public String getPort_plc() {
        return port_plc;
    }
    public void setPort_plc(String port_plc) {
        this.port_plc = port_plc;
    }
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    
}
