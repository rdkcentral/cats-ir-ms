
package com.cats.ir;

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

/**
 * This class represents the different ir devices that are used to send IR. Each
 * device could have a number of ports to which individual settops are
 * connected. These ports are represented by {@link IRDevicePort} . The Device
 * port will in turn use a communicator {@link RedRatHubCommunicator} or {@Link
 * GC6Communicator} to actually blast an IR command.
 */
public abstract class IRDevice {
    // these values can be set/accessed by the concrete implementation.
    protected String id;
    protected String deviceIPAddr;
    protected String deviceModule;
    protected IRCommunicatorManager communicatorManager;
    protected List<IRDevicePort> devicePorts;
    protected IRHardwareEnum deviceType;

    public abstract boolean init();

    public abstract boolean uninit();

    /**
     * Get device Id.
     *
     * @return id;
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of this device
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get device Id.
     *
     * @return id;
     */
    public IRHardwareEnum getDeviceType() {
        return deviceType;
    }

    /**
     * Get device IP.
     *
     * @return id;
     */
    public String getDeviceIp() {
        return deviceIPAddr;
    }

    /**
     * Set the id of this device
     *
     * @param id
     */
    public void setDeviceIp(String deviceIPAddr) {
        this.deviceIPAddr = deviceIPAddr;
    }

    /**
     * Set the singleton {@link IRCommunicatorManager}.
     * The CommunicatorManger is responsible for managing connections to a hub
     * <p>
     * Currently no pooling of connections is done by the communicator manager.
     *
     * @param communicatorManager
     */
    public void setIRCommunicatorManager(IRCommunicatorManager communicatorManager) {
        this.communicatorManager = communicatorManager;
    }

    public IRCommunicatorManager getCommunicatorManager() {
        return communicatorManager;
    }

    /**
     * Get a list of all {@link IRPort} on this device.
     *
     * @return list of all ports.
     */
    public List<IRDevicePort> getIrPorts() {
        return devicePorts;
    }

    /**
     * Get a port corresponding to the port number.
     *
     * @param portNumber
     * @return the {@link IRPort}
     */
    public abstract IRDevicePort getPort(int portNumber);

    public String getDeviceModule() {
        return deviceModule;
    }

    public void setDeviceModule(String deviceModule) {
        this.deviceModule = deviceModule;
    }
}
