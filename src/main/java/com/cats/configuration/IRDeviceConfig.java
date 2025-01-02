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

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a block of IR devices with a starting ip, port, and
 * number of devices in the block (count).
 * Retrieved from ir-ms.yml.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Service
public class IRDeviceConfig {

    //type is the type of device
    protected String type;

    //host is the starting ip address of the block
    protected String host;

    //port is the starting port of the block
    protected Integer port;

    //count is the number of devices in the block
    public Integer count;

    //maxPorts is the maximum number of ports
    protected Integer maxPorts;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("maxPorts")
    public Integer getMaxPorts() {
        return maxPorts != null ? maxPorts : 16;
    }

    public void setMaxPorts(Integer maxPorts) {
        this.maxPorts = maxPorts;
    }

}
