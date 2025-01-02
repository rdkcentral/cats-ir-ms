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

import java.util.List;

/**
 * Represents the health data for the GC Dispatcher.
 */
@AllArgsConstructor
@NoArgsConstructor
public class GCDispatcherHealthDataBean {
    @JsonProperty("devices")
    private List<GCDeviceBean> gcDevices = null;
    // This is a placeholder for the IRDB data.
    private IrdbBean irdb;

    @JsonProperty("devices")
    public List<GCDeviceBean> getGCDevices() {
        return gcDevices;
    }

    @JsonProperty("devices")
    public void setGCDevices(List<GCDeviceBean> gcDevices) {
        this.gcDevices = gcDevices;
    }

    @JsonProperty("irdb")
    public IrdbBean getIrdb() {
        return irdb;
    }

    @JsonProperty("irdb")
    public void setIrdb(IrdbBean irdb) {
        this.irdb = irdb;
    }

}