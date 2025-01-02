package com.cats.ir.gchealth;

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class GCDeviceBean.
 * It represents a Global Cache device.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class GCDeviceBean {

    // The hostname or IP address of the Global Cache device
    private String host;

    // The port number on which the device is listening
    private int port;

    // The number of active connections to the device
    private int activeConnections;

    // The firmware version of the device
    private String version;

    // A list of modules associated with the device
    private List<ModuleBean> modules = null;

    // The health status of the device
    private GCDeviceHealthBean gcHealth;

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("port")
    public int getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty("active_connections")
    public int getActiveConnections() {
        return activeConnections;
    }

    @JsonProperty("active_connections")
    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("modules")
    public List<ModuleBean> getModules() {
        return modules;
    }

    @JsonProperty("modules")
    public void setModules(List<ModuleBean> modules) {
        this.modules = modules;
    }

    @JsonProperty("health")
    public GCDeviceHealthBean getGcHealth() {
        return gcHealth;
    }

    @JsonProperty("health")
    public void setGcHealth(GCDeviceHealthBean gcHealth) {
        this.gcHealth = gcHealth;
    }
}