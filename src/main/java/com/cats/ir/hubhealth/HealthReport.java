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

import com.cats.ir.gchealth.GCDeviceBean;
import com.cats.ir.gchealth.GCDispatcherHealthBean;
import com.cats.ir.gchealth.GCDispatcherHealthDataBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the health report for various devices and services.
 * This class includes information about the device or service, its health status, and additional metadata.
 */
@Data
@Slf4j
public class HealthReport {
    String deviceId;
    String entity;
    Boolean isHealthy;
    String remarks;
    String host;
    Map<String, String> version = new HashMap<>();
    Map<String, String> metadata = new HashMap<>();

    public HealthReport(RedRatDeviceBean device) {
        this.entity = device.getType();
        this.isHealthy = device.getStatus().equalsIgnoreCase("connected");
        this.host = device.getIp();
        this.version.put("firmware", device.getFirmwareVersion());
        this.metadata.put("mac", device.getMac());
        this.metadata.put("hardwareType", device.getHardwareType());
    }

    public HealthReport(GCDeviceBean gcDevice,int count) {
        this.entity = "iTach"+ count;
        this.isHealthy = gcDevice.getGcHealth().isAvailable();
        this.host = gcDevice.getHost();
        this.deviceId = String.valueOf(count);
        this.version.put("Firmware version", gcDevice.getVersion());
        this.metadata.put("port", String.valueOf(gcDevice.getPort()));
        this.metadata.put("Active Connections", String.valueOf(gcDevice.getActiveConnections()));
        this.metadata.put("Modules", objectToString(gcDevice.getModules()));
        if (gcDevice.getGcHealth().getErrors().isEmpty()){
            this.metadata.put("Errors", String.valueOf(gcDevice.getGcHealth().getErrors())) ;
        }

    }

    public HealthReport(HubHealthBean hubHealthBean) {
        this.isHealthy = hubHealthBean.getHubUp();
        this.version = hubHealthBean.getHubVersion();
        this.entity = "RedRatHub";
    }

    public HealthReport(GCDispatcherHealthBean gcHealthBean) {
        this.isHealthy = gcHealthBean.getIsHealthy();
        this.entity = "GC Dispatcher Service";
        GCDispatcherHealthDataBean result = gcHealthBean.getResult();
        if (result != null) {
            if (result.getIrdb()!=null){
                this.metadata.put("irdbDatasetLoaded", objectToString(result.getIrdb().isDatasetLoaded()));
                this.metadata.put("irDataset", objectToString(result.getIrdb().getIrDevices()));
            }

        } else {
            setRemarks("Did not receive health status from GCDispatcherService");
        }
    }

    public String objectToString(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Could not process object";
        }
    }
}
