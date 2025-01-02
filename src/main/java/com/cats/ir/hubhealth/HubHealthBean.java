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

import java.util.List;
import java.util.Map;

/**
 * Represents details about RedRatHub
 * and the RedRat devices associated with it.
 */
public class HubHealthBean {
    protected String version;

    // The version of the RedRatHub.
    private Map<String, String> hubVersion;

    // The list of keysets.
    private List<String> keysets;

    // The list of RedRat devices.
    private List<RedRatDeviceBean> devices;

    // Indicates whether the RedRatHub is up.
    private Boolean hubUp;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHubVersion() {
        return hubVersion;
    }

    public void setHubVersion(Map<String, String> hubVersion) {
        this.hubVersion = hubVersion;
    }

    public List<String> getKeysets() {
        return keysets;
    }

    public void setKeysets(List<String> keysets) {
        this.keysets = keysets;
    }

    public List<RedRatDeviceBean> getDevices() {
        return devices;
    }

    public void setDevices(List<RedRatDeviceBean> devices) {
        this.devices = devices;
    }

    public Boolean getHubUp() {
        return hubUp;
    }

    public void setHubUp(Boolean hubUp) {
        this.hubUp = hubUp;
    }


}
