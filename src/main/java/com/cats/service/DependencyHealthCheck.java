package com.cats.service;

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

import com.cats.configuration.IRConfiguration;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service to check the health of dependencies.
 */
@Service
@Slf4j
public class DependencyHealthCheck {

    private Boolean checkGCDispatcherHealth = null;
    private Boolean checkHubHealth = null;

    @Autowired
    IRConfiguration irConfiguration;

    @PostConstruct
    public void init() {
        String gcDispatcherApiBase = irConfiguration.getGcDispatcherApiBase();
        String hubIp = irConfiguration.getRedRatHubHost();
        String hubPort = irConfiguration.getRedRatHubPort();


        this.checkGCDispatcherHealth = irConfiguration.getNormalizedIrDevices().stream().anyMatch(irDeviceConfig -> irDeviceConfig.getType().equalsIgnoreCase("itach") || irDeviceConfig.getType().equalsIgnoreCase("gc100"));
        this.checkHubHealth = hubIp != null && hubPort != null;
        log.info("Found Available IR Dependencies");
    }

    public boolean checkGCDispatcherHealth() {
        return this.checkGCDispatcherHealth;
    }

    public boolean checkHubHealth() {
        return this.checkHubHealth;
    }
}
