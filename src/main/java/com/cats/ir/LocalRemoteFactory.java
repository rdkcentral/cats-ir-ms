
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LocalRemoteFactory implements RemoteFactory {
    protected Logger logger = LoggerFactory.getLogger(LocalRemoteFactory.class);
    @Autowired
    protected IRDeviceManager manager;


    /**
     * Provide additional method to handle local USB connected devices with deviceName;
     * @param type - Type of IR device to return.
     * @param host - Device host.
     * @param keySet - Remote keyset to use when sending keys.
     * @param port - Port of IR device to use.
     * @return - Remote to perform key transmission.
     */
    @Override
    public Remote getRemote(IRHardwareEnum type, String host, String keySet, int port) {
        return getRemote(type, host, keySet, port, null);
    }

    /**
     * Provide additional method to handle local USB connected devices with deviceName;
     * @param type - Type of IR device to return.
     * @param host - Device host.
     * @param keySet - Remote keyset to use when sending keys.
     * @param port - Port of IR device to use.
     * @param deviceName - Optional deviceName for non-host devices.
     * @return - Remote to perform key transmission.
     */
    @Override
    public Remote getRemote(IRHardwareEnum type, String host, String keySet, int port, String deviceName) {
        IRDevice irDevice = manager.getIRDevice(type, host, deviceName);
        IRDevicePort irPort = irDevice.getPort(port);
        LocalRemote remote = new LocalRemote(irPort, keySet);
        return remote;
    }

    public IRDeviceManager getIRDeviceManager() {
        return manager;
    }
}
