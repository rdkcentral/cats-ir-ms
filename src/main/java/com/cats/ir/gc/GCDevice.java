package com.cats.ir.gc;

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

import com.cats.ir.IRDevice;
import com.cats.ir.IRDevicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GCDevice.
 * This class is to manages and interact with the Global Cache device.
 */
public abstract class GCDevice extends IRDevice {

    private static final Logger logger = LoggerFactory.getLogger(GCDevice.class);
    protected String getGcDispatcherApiBase;

    public GCDevice(String id, String getGcDispatcherApiBase) {
        this.id = id;
        this.getGcDispatcherApiBase = getGcDispatcherApiBase;
    }

    @Override
    public String toString() {
        return "device " + getId();
    }

    @Override
    public IRDevicePort getPort(int portNumber) {
        IRDevicePort retVal = null;
        if (devicePorts != null && !devicePorts.isEmpty()) {
            for (IRDevicePort irPort : devicePorts) {
                if (irPort.getPortNumber() == portNumber) {
                    retVal = irPort;
                    break;
                }
            }
        }
        logger.debug("getPort " + portNumber + " from irDevice " + deviceIPAddr + " : " + retVal);
        return retVal;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object instanceof GCDevice) {
            if (((GCDevice) object).getId().equals(this.getId())) {
                isEqual = true;
            }
        }
        return isEqual;
    }

    public String getGetGcDispatcherApiBase() {
        return getGcDispatcherApiBase;
    }

    public void setGetGcDispatcherApiBase(String getGcDispatcherApiBase) {
        this.getGcDispatcherApiBase = getGcDispatcherApiBase;
    }

}
