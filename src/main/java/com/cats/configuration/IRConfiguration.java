package com.cats.configuration;

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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Represents the deserialized ir-ms.yml file.
 *
 */
@Component
@ConfigurationProperties
@PropertySource(value = "file:./irms/ir-ms.yml", factory = YamlPropertySourceFactory.class)
public class IRConfiguration {

    //version is the version of the configuration
    public int version;

    //redRatHubHost is the host of the RedRatHub
    public String redRatHubHost;

    //redRatHubPort is the port of the RedRatHub
    public String redRatHubPort;

    //gcDispatcherApiBase is the base URL for the GCDispatcher API
    public String gcDispatcherApiBase;

    //irDevices is a list of IRDeviceConfig objects
    public List<IRDeviceConfig> irDevices;

    @Deprecated
    public List<IRDeviceConfig> irNetBox;

    //doMock is a boolean that determines if the application should mock
    public boolean doMock = false;

    //mockDelay is the delay for the mock
    public int mockDelay;

    //redRatHubReadTimeout is the read timeout for the RedRatHub
    public String redRatHubReadTimeout;

    //gcDispatcherConfigLocation is the location of the GCDispatcher configuration
    public String gcDispatcherConfigLocation;


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getRedRatHubHost() {
        return redRatHubHost;
    }

    public void setRedRatHubHost(String redRatHubHost) {
        this.redRatHubHost = redRatHubHost;
    }

    public String getRedRatHubPort() {
        return redRatHubPort;
    }

    public void setRedRatHubPort(String redRatHubPort) {
        this.redRatHubPort = redRatHubPort;
    }

    public String getGcDispatcherApiBase() {
        return gcDispatcherApiBase;
    }

    public void setGcDispatcherApiBase(String gcDispatcherApiBase) {
        this.gcDispatcherApiBase = gcDispatcherApiBase;
    }

    public List<IRDeviceConfig> getIrDevices() {
        return irDevices;
    }

    public void setIrDevices(List<IRDeviceConfig> irDevices) {
        this.irDevices = irDevices;
    }

    @Deprecated
    public List<IRDeviceConfig> getIrNetBox(){ return irNetBox; }

    @Deprecated
    public void setIrNetBox(List<IRDeviceConfig> irNetBox) {
        this.irNetBox = irNetBox;
    }

    public boolean isDoMock() {
        return doMock;
    }

    public void setDoMock(boolean doMock) {
        this.doMock = doMock;
    }

    public int getMockDelay() {
        return mockDelay;
    }

    public void setMockDelay(int mockDelay) {
        this.mockDelay = mockDelay;
    }

    public String getRedRatHubReadTimeout() {
        return redRatHubReadTimeout;
    }

    public void setRedRatHubReadTimeout(String redRatHubReadTimeout) {
        this.redRatHubReadTimeout = redRatHubReadTimeout;
    }

    public String getGcDispatcherConfigLocation() {
        return gcDispatcherConfigLocation;
    }

    public void setGcDispatcherConfigLocation(String gcDispatcherConfigLocation) {
        this.gcDispatcherConfigLocation = gcDispatcherConfigLocation;
    }

    /**
     * Generates a list of normalized device configs, i.e., count=1 for each item based on either
     * irDevices or irNetBoxDevies (irDevices gets precedence)
     *
     * @return A list of normalized device configs
     */

    public List<IRDeviceConfig> getNormalizedIrDevices() {
        List<IRDeviceConfig> source; // either irDevices or irNetBoxDevices
        List<IRDeviceConfig> result = new ArrayList<IRDeviceConfig>();
        String defaultType = null;
        if (irDevices != null) {
            source = new ArrayList<>(irDevices);
        } else {
            defaultType = "irNetBox";
            source = new ArrayList<>(irNetBox);
        }
        for (IRDeviceConfig irDevice : source) {
            int count = irDevice.count != null ? irDevice.count : 1;
            String host = irDevice.host;
            for (int i = 0; i < count; i++) {
                IRDeviceConfig config = new IRDeviceConfig();
                config.type = irDevice.type != null ? irDevice.type : defaultType;
                if (i == 0) {
                    config.host = host;
                } else {
                    host = calculateNextHost(host);
                    config.host = host;
                }
                config.count = 1;
                config.port = irDevice.port;
                config.maxPorts = irDevice.maxPorts;
                result.add(config);
            }
        }

        return result;
    }

    /**
     * Increments the ip address by 1 based on the count of
     * the IRDeviceConfig object. Assumes IPv4.
     *
     * @param host The original ip.
     * @return The incremented ip.
     */
    private String calculateNextHost(String host) {
        host = host.replaceAll("http://", "");
        String[] splitHost = host.split("\\.");
        for (int i = splitHost.length - 1; i >= 0; i--) {
            int octet = Integer.parseInt(splitHost[i]);
            if (octet + 1 < 255) {
                splitHost[i] = String.valueOf(octet + 1);
                break;
            } else {
                splitHost[i] = "0";
            }
        }

        String newHost = "";
        for (String octet : splitHost) {
            newHost += octet + ".";
        }

        newHost = newHost.substring(0, newHost.length() - 1);
        return newHost;
    }
}
