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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents the health data for the GC Dispatcher.
 * This class includes information about the module, the number of ports, and the type of the module.
 */
@NoArgsConstructor
@AllArgsConstructor
public class ModuleBean {

    private int module;
    private int ports;
    private String type;

    @JsonProperty("module")
    public int getModule() {
        return module;
    }

    @JsonProperty("module")
    public void setModule(int module) {
        this.module = module;
    }

    @JsonProperty("ports")
    public int getPorts() {
        return ports;
    }

    @JsonProperty("ports")
    public void setPorts(int ports) {
        this.ports = ports;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }
}
