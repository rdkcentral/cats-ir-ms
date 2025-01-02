package com.cats.dto;

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

import com.cats.configuration.GCDispatcherConfig;
import com.cats.configuration.IRDeviceConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GCDispatcherConfigDto {

    //apiBase is the base URL for the GCDispatcher API
    private String apiBase;

    //irDevices is a list of IRDeviceConfig objects
    private List<IRDeviceConfig> irDevices;

    public GCDispatcherConfigDto(String apiBase, List<IRDeviceConfig> irDevices) {
        this.apiBase = apiBase;
        this.irDevices = irDevices;
    }

    public GCDispatcherConfigDto(GCDispatcherConfig config) {
        this.apiBase = config.getApiBase();
        this.irDevices = config.getIrDevices();
    }

}
