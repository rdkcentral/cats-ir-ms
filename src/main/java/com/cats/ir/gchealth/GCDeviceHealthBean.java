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
 * The Class GCDeviceHealthBean.
 * It represents the health status of a Global Cache device.
 */
@NoArgsConstructor
@AllArgsConstructor
public class GCDeviceHealthBean {

    // A flag indicating whether the device is available
    private boolean available;

    // A list of errors associated with the device
    private List<Object> errors = null;

    @JsonProperty("available")
    public boolean isAvailable() {
        return available;
    }

    @JsonProperty("available")
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @JsonProperty("errors")
    public List<Object> getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }
}
