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
@NoArgsConstructor
@AllArgsConstructor
public class IrdbBean {
    // Indicates whether the IRDB dataset is loaded.
    private boolean datasetLoaded;

    // The list of IR devices.
    private List<String> irDevices = null;

    @JsonProperty("dataset_loaded")
    public boolean isDatasetLoaded() {
        return datasetLoaded;
    }

    @JsonProperty("dataset_loaded")
    public void setDatasetLoaded(boolean datasetLoaded) {
        this.datasetLoaded = datasetLoaded;
    }

    @JsonProperty("ir_devices")
    public List<String> getIrDevices() {
        return irDevices;
    }

    @JsonProperty("ir_devices")
    public void setIrDevices(List<String> irDevices) {
        this.irDevices = irDevices;
    }
}
