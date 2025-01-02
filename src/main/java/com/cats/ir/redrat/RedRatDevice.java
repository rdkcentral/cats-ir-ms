
package com.cats.ir.redrat;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRDevice;
import com.cats.ir.IRDevicePort;

/**
 * This class represents an abstract for all RedRat devices.
 */
public abstract class RedRatDevice extends IRDevice {

    Logger logger = LoggerFactory.getLogger(RedRatDevice.class);

    public RedRatDevice(String id) {
        this.id = id;
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
        if (object instanceof RedRatDevice) {
            if (((RedRatDevice) object).getId().equals(this.getId())) {
                isEqual = true;
            }
        }
        return isEqual;
    }
}
