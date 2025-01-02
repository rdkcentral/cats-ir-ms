package com.cats.ir.hubhealth;

/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Represents RedRat devices.
 */
public class RedRatDeviceBean implements Comparable {

    // The type of the RedRat device.
    private String type;

    // The MAC address of the RedRat device.
    private String mac;

    // The IP address of the RedRat device.
    private String ip;

    // The status of the RedRat device.
    private String status;

    // The hardware type of the RedRat device.
    private String hardwareType;

    // The firmware version of the RedRat device.
    private String firmwareVersion;

    public RedRatDeviceBean(String type, String mac, String ip, String status) {
        this.type = type;
        this.mac = mac;
        this.ip = ip;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(String hardwareType) {
        this.hardwareType = hardwareType;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * Used to sort lists of RedRatDeviceBean by ascending IP address.
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof RedRatDeviceBean deviceBean) {
            return (this.ip).compareTo(deviceBean.getIp());
        }

        return -1;
    }
}
