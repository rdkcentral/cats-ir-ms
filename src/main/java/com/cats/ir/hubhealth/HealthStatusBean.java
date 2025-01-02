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

import com.cats.ir.gchealth.GCDispatcherHealthBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Represents the health status of the various devices and services.
 * This class includes information about the health status of the devices and services,
 * the version of the microservice, and additional metadata.
 */
@Data
@Slf4j
public class HealthStatusBean {

    // The version of the microservice.
    Map<String, String> version = new HashMap<>();

    // Indicates whether the microservice is healthy.
    Boolean isHealthy;

    // The health status of the hardware devices.
    List<HealthReport> hwDevicesHealthStatus;

    // The health status of the dependencies.
    List<HealthReport> dependenciesHealthStatus;

    public HealthStatusBean(HubHealthBean hubHealthBean, GCDispatcherHealthBean gcDispatcherHealthBean) {
        this.version.put("MS_VERSION", getMicroServiceVersion());
        this.hwDevicesHealthStatus = new ArrayList<>();
        this.isHealthy = false;
        if (hubHealthBean != null && gcDispatcherHealthBean != null) {
            this.isHealthy = hubHealthBean.getHubUp() && gcDispatcherHealthBean.getIsHealthy();
        } else if (hubHealthBean != null) {
            this.isHealthy = hubHealthBean.getHubUp();
        } else if (gcDispatcherHealthBean != null) {
            this.isHealthy = gcDispatcherHealthBean.getIsHealthy();
        }

        this.dependenciesHealthStatus = new ArrayList<>();

        if (hubHealthBean != null) {
            hubHealthBean.getDevices().forEach(device -> {
                this.hwDevicesHealthStatus.add(new HealthReport(device));
                if (!device.getStatus().equalsIgnoreCase("connected")) {
                    this.isHealthy = false;
                }
            });

            this.dependenciesHealthStatus.add(new HealthReport(hubHealthBean));
        }

        if (gcDispatcherHealthBean != null) {
            AtomicInteger counter = new AtomicInteger(0);
            gcDispatcherHealthBean.getResult().getGCDevices().forEach(gcDeviceBean -> {
                this.hwDevicesHealthStatus.add(new HealthReport(gcDeviceBean, counter.incrementAndGet()));
            });
            this.dependenciesHealthStatus.add(new HealthReport(gcDispatcherHealthBean));
        }
    }

    public String getMicroServiceVersion() {
        try {
            Manifest manifest = new Manifest(getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
            Attributes mainAttributes = manifest.getMainAttributes();
            String version = mainAttributes.getValue("Implementation-Version");
            if (version == null || version.isEmpty()) {
                version = "development";
            }
            return version;
        } catch (IOException e) {
            log.error("Error retrieving version");
        }
        return "NA";
    }

}