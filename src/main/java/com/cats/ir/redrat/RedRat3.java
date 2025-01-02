
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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRCommunicatorManager;
import com.cats.ir.IRDevicePort;
import com.cats.ir.IRHardwareEnum;

/**
 * Represents the RedRat3 device.
 */
public class RedRat3 extends RedRatDevice {
    private static final Logger logger = LoggerFactory.getLogger(RedRat3.class);
    // The number of ports of an  RedRat3 device would be 1
    public static final int RED_RAT3_PORT = 1;
    private static final String DEFAULT_REDRAT3_NAME = "No name 9130";
    private String deviceName;

    /**
     * @param id
     * @param ipAddress
     * @param communicatorManager
     * Constructor with out deviceName as parameter, called when there is no deviceName
     */
    public RedRat3(String id, String ipAddress, IRCommunicatorManager communicatorManager) {
        this(DEFAULT_REDRAT3_NAME, id, ipAddress, communicatorManager);
    }

    /**
     *
     * @param deviceName
     * @param id
     * @param ipAddress
     * @param communicatorManager
     * Constructor which takes deviceName as parameter so that multiple RedRat devices can be accessed at once 
     */
    public RedRat3(String deviceName, String id, String ipAddress, IRCommunicatorManager communicatorManager) {

        super(id);
        this.deviceIPAddr = ipAddress;
        this.deviceType = IRHardwareEnum.REDRAT3;
        this.communicatorManager = communicatorManager;
        this.setDeviceName(deviceName);
        init();
    }

    /**
     * Equals and hash code critical for redrat discovery and blacklisting
     * mechanism
     */
    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object instanceof RedRat3) {

            if (super.equals(object) && ((RedRat3) object).deviceIPAddr.equals(deviceIPAddr)) {
                isEqual = true;
            }
        }
        return isEqual;
    }

    /**
     * Equals and hash code critical for redrat discovery and blacklisting
     * mechanism
     */
    @Override
    public int hashCode() {
        return deviceIPAddr.hashCode();
    }

    /**
     * Initializes the ports on an IRNetBoxPro device.
     */
    public boolean init() {
        logger.trace("Initializing the single RedRat3 port");
        devicePorts = new ArrayList<IRDevicePort>(1);
        devicePorts.add(new RedRat3Port(this, communicatorManager));
        return true;
    }

    @Override
    public boolean uninit() {
        return false;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
