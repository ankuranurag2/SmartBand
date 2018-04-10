package com.ankuranurag2.smartband;

public class BTDevice {

    private String deviceName;
    private String deviceAddr;

    public BTDevice() {
    }

    public BTDevice(String deviceName, String deviceAddr) {
        this.deviceName = deviceName;
        this.deviceAddr = deviceAddr;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddr() {
        return deviceAddr;
    }

    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }
}
