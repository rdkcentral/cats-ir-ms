
package com.cats.ir.manager;

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
import com.cats.ir.IRCommunicatorManager;
import com.cats.ir.IRDevice;
import com.cats.ir.IRDeviceManager;
import com.cats.ir.IRHardwareEnum;
import com.cats.ir.gc.gc100.GC100;
import com.cats.ir.gc.itach.iTach;
import com.cats.ir.redrat.IrNetBoxPro;
import com.cats.ir.redrat.RedRat3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

/**
 * IRDevice manager responsible for Local instance of IRLibrary
 */
@Service
public class LocalIRDeviceManager implements IRDeviceManager {
    @Autowired
    protected IRCommunicatorManager irCommManager;
    @Autowired
    IRConfiguration irConfiguration;
    private HashMap<String, IRDevice> deviceMap;
    Logger logger = LoggerFactory.getLogger(LocalIRDeviceManager.class);

    private LocalIRDeviceManager() {
        deviceMap = new HashMap<String, IRDevice>(10);
    }

    @Inject
    public LocalIRDeviceManager(IRCommunicatorManager irCommManager) {
        this();
        this.irCommManager = irCommManager;
    }

    /**
     * (non-Javadoc)
     *
     * @param deviceURI - Device locator.
     * @return IRDevice retrieved based on device URI.
     * @see IRDeviceManager#getIRDevice(java.net.URI)
     */
    @Override
    public IRDevice getIRDevice(URI deviceURI) {
        return getIRDevice(deviceURI, null);
    }

    /**
     * (non-Javadoc)
     *
     * @param deviceURI  - Device specific URI locator.
     * @param deviceName - Device name to handle local devices that potentially don't have IP/hostname.
     * @return IRDevice based on URI and deviceName.
     * @see IRDeviceManager#getIRDevice(java.net.URI, java.lang.String)
     * Method to get the IRDevice by giving URI and the deviceName
     */
    @Override
    public IRDevice getIRDevice(URI deviceURI, String deviceName) {
        if (deviceURI == null) {
            throw new IllegalArgumentException("The device URI cannot be null");
        }
        String deviceHost = deviceURI.getHost();
        IRHardwareEnum deviceType = IRHardwareEnum.getByValue(deviceURI.getScheme());
        return getIRDevice(deviceType, deviceHost, deviceName);
    }

    @Override
    public IRDevice getIRDevice(IRHardwareEnum deviceType, String deviceHostIP) {
        return getIRDevice(deviceType, deviceHostIP, null);
    }

    @Override
    public IRDevice getIRDevice(IRHardwareEnum deviceType, String deviceHostIP, String deviceName) {
        IRDevice device = getDeviceFromMap(deviceName, deviceHostIP);

        if (device == null) {
            switch (deviceType) {
                case GC100:
                case GC100_6:
                case GC100_12:
                    device = new GC100(deviceHostIP, deviceHostIP, irConfiguration.gcDispatcherApiBase);
                    deviceMap.put(deviceHostIP, device);
                    logger.info("Instatiated GC device:{}", deviceHostIP);
                    break;
                case ITACH:
                    device = new iTach(deviceHostIP, deviceHostIP, irConfiguration.gcDispatcherApiBase);
                    deviceMap.put(deviceHostIP, device);
                    logger.info("Instatiated iTach device:{}", deviceHostIP);
                    break;
                case IRNETBOXPRO3:
                    device = new IrNetBoxPro(deviceHostIP, deviceHostIP, irCommManager);
                    deviceMap.put(deviceHostIP, device);
                    logger.info("Instatiated IrNetBoxPro device:{}", deviceHostIP);
                    break;
                case REDRAT3:
                    if (deviceName == null || deviceName.isEmpty()) {
                        device = new RedRat3(deviceHostIP, deviceHostIP, irCommManager);
                        deviceMap.put(deviceHostIP, device);
                    } else {
                        device = new RedRat3(deviceName, deviceHostIP, deviceHostIP, irCommManager);
                        deviceMap.put(deviceName, device);
                    }
                    logger.info("Instatiated RedRat3 device:{}", deviceHostIP);
                    break;
                default:
                    logger.error("The type:[{}] of IR device is not supported ", deviceType);
                    break;
            }

        }
        return device;
    }

    /**
     * Method to get the IRDevice from deviceMap
     */
    private IRDevice getDeviceFromMap(String name, String ip) {
        IRDevice dev = null;
        if (name != null && !name.isEmpty()) {
            dev = deviceMap.get(name);
        }
        if (dev == null) {
            dev = deviceMap.get(ip);
        }
        return dev;
    }

    @Override
    public Collection<IRDevice> getAllAvailableDevices() {
        return deviceMap.values();

    }
}
