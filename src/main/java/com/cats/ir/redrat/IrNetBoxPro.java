
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

import java.io.IOException;
import java.util.ArrayList;

import com.cats.ir.IRCommunicator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRCommunicatorManager;
import com.cats.ir.IRDevicePort;
import com.cats.ir.IRHardwareEnum;
import com.cats.utils.TelnetConnection;
import org.springframework.beans.factory.annotation.Value;


/**
 * Represents the IrNetBoxPro device.
 */

@Slf4j
public class IrNetBoxPro extends RedRatDevice {
    private static final Logger logger = LoggerFactory.getLogger(IrNetBoxPro.class);

    // The number of ports of an IrNetBoxPro device would be 16
    private static final int IRNETBOX_PRO_MAXPORTS = 16;

    @Value("${redrat.hub.port}")
    private int redratHubPort;


    public IrNetBoxPro(String id, String ipAddress, IRCommunicatorManager communicatorManager) {
        super(id);
        this.deviceIPAddr = ipAddress;
        this.deviceType = IRHardwareEnum.IRNETBOXPRO3;
        this.communicatorManager = communicatorManager;
        init();
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

    /**
     * Equals and hash code critical for redrat discovery and blacklisting
     * mechanism
     */
    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object instanceof IrNetBoxPro) {

            if (super.equals(object) && ((IrNetBoxPro) object).deviceIPAddr.equals(deviceIPAddr)) {
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
        devicePorts = new ArrayList<IRDevicePort>(IRNETBOX_PRO_MAXPORTS);
        for (int i = 1; i <= IRNETBOX_PRO_MAXPORTS; i++) {
            IRDevicePort port = new IrNetBoxProPort(i, this, communicatorManager);
            devicePorts.add(port);
        }

        String command = String.format("hq=\"add irnetbox\" ip=\"%s\"", this.deviceIPAddr);

        try {
            IRCommunicator communicator = communicatorManager.getCommunicator(this);
            if (communicator != null) {
                communicator.sendCommand(command);
            } else {
                log.error("No IRCommunicator available for this device");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public boolean uninit() {
        return false;
    }
}
