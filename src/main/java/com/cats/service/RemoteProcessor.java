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

import java.io.IOException;
import java.util.List;

import com.cats.configuration.GCDispatcherConfig;
import com.cats.configuration.IRConfiguration;
import com.cats.configuration.IRDeviceConfig;
import com.cats.ir.IRHardwareEnum;
import com.cats.ir.Remote;
import com.cats.ir.RemoteFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Determines which device and port have been requested and returns
 * a remote to communicate with that device/port.
 */
@Service
public class RemoteProcessor {
    @Autowired
    private SlotMappingService mappingService;
    @Autowired
    private RemoteFactory remoteFactory;
    public List<IRDeviceConfig> irDeviceList;
    @Autowired
    public IRConfiguration irConfig;


    @PostConstruct
    public void init() throws IOException {
        irDeviceList = irConfig.getNormalizedIrDevices();
        if(irConfig.gcDispatcherApiBase != null && !irConfig.gcDispatcherApiBase.isEmpty()){
            GCDispatcherConfig.writeGCDispatcherConfigYaml(irDeviceList, irConfig.gcDispatcherApiBase);
        }
    }


    /**
     * Determines whether requested slot is mapped to a valid device/port.
     *
     * @param slot The requested slot (which references a device/port).
     *
     * @return true, if valid, else false.
     */
    public boolean validateSlot(String slot) {
        try {
            int[] deviceAndPort = findDeviceAndPortMapping(slot);
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether requested device is valid.
     *
     * @return true, if valid, else false.
     */
    public boolean validateDevice(String device) {
        int deviceNum = Integer.parseInt(device);
        deviceNum = deviceNum - 1; // To make it 0-based.
        return !(deviceNum < 0 || deviceNum >= irDeviceList.size());
    }

    /**
     * Determines the device and port for a given slot.
     *
     * @return int[] containing deviceId and irPort.
     */
    int[] findDeviceAndPortMapping(String slot) {
        // Return tuple (deviceId, irPort)... NOTE: both deviceId and irPort are 1-indexed
        int[] result = new int[2];
        if (!mappingService.getMappings().getMappings().isEmpty()) {
            String deviceMappings = mappingService.getMappings().getMapping(slot);
            String[] deviceAndPortMapping = deviceMappings.split(":");
            int deviceId = Integer.valueOf(deviceAndPortMapping[0]);
            int irPort = Integer.valueOf(deviceAndPortMapping[1]);
            result[0] = deviceId;
            result[1] = irPort;
            return result;
        } else {
            int deviceId = 1;
            int slotOffset = Integer.valueOf(slot);
            for (IRDeviceConfig currentDevice : irDeviceList) {
                if (slotOffset <= currentDevice.getMaxPorts()) {
                    result[0] = deviceId;
                    result[1] = slotOffset;
                    //log.info("DEVICE ID: {} IR PORT: {}", deviceId, slotOffset);
                    return result;
                } else {
                    deviceId += 1;
                    slotOffset -= currentDevice.getMaxPorts();
                }
            }
            throw new IllegalStateException("Couldn't find the device/port");
        }
    }

    /**
     * Determines whether requested device has requested port.
     *
     * @return true, if valid, else false.
     */
    public boolean deviceHasPort(String deviceId, String port) {
        int portNum = Integer.parseInt(port);
        IRDeviceConfig irDevice = irDeviceList.get(Integer.valueOf(deviceId) - 1);
        return ((1 <= Integer.valueOf(port)) && (Integer.valueOf(port) <= irDevice.getMaxPorts()));
    }

    /**
     * Determines the maximum number of ports for a given device.
     *
     * @return int containing maxPorts.
     */
    public Integer findMaxPortsOfDevice(String device) {
        Integer maxPorts = 0;
        if (validateDevice(device)) {
            int deviceNum = Integer.parseInt(device) - 1; // To make it 0-based.
            IRDeviceConfig irDevice = irDeviceList.get(deviceNum);
            maxPorts = irDevice.getMaxPorts();
            assert maxPorts != null;
        }

        return maxPorts;
    }

    /**
     * Determines the number of slots available across all IR devices
     *
     * @return int containing number of slots.
     */
    public int numSlots() {
        int result = 0;
        for (IRDeviceConfig irDevice : irDeviceList) {
            result += irDevice.getMaxPorts();
        }
        return result;
    }

    /**
     * Determines the number of IR devices available
     *
     * @return int containing number of devices.
     */
    public int numDevices() {
        return irDeviceList.size();
    }

    /**
     * Determines the IR remote type based on the device type
     *
     * @return IRHardwareEnum containing the IR remote type.
     */
    private IRHardwareEnum findIrRemoteType(String irDeviceType) {
        IRHardwareEnum irRemoteType = IRHardwareEnum.IRNETBOXPRO3;

        if (irDeviceType != null) {
            switch (irDeviceType) {
                case "itach":
                    irRemoteType = IRHardwareEnum.ITACH;
                    break;
                case "gc100":
                    irRemoteType = IRHardwareEnum.GC100;
                    break;
                case "gc100_12":
                    irRemoteType = IRHardwareEnum.GC100_12;
                    break;
                case "redrat":
                    irRemoteType = IRHardwareEnum.REDRAT3;
                    break;
                case "irNetBox":
                default:
                    irRemoteType = IRHardwareEnum.IRNETBOXPRO3;
                    break;
            }
        }

        return irRemoteType;
    }

    /**
     * Determines the remote for a given rack, slot, and keyset.
     *
     * @return Remote containing the remote.
     */
    public Remote getRemote(String slot, String keySet) {
        Remote remote;
        if (irConfig.getNormalizedIrDevices() == null || irConfig.getNormalizedIrDevices().isEmpty()) {
            throw new IllegalStateException("No IR devices in config");
        }
        int[] deviceAndPort = findDeviceAndPortMapping(slot);
        int deviceId = deviceAndPort[0];
        int irPort = deviceAndPort[1];
        return getRemote(deviceId, irPort, keySet);
    }

    /**
     * Determines the remote for a given device, port, and keyset.
     *
     * @return Remote containing the remote.
     */
    public Remote getRemote(int deviceId, int irPort, String keyset) {
        if (deviceId <= 0 || deviceId > irConfig.getNormalizedIrDevices().size()) {
            throw new IllegalArgumentException("Requested device[" + deviceId + "] > size[" + irDeviceList.size() + "] Invalid");
        }
        //Make sure to handle the zero indexed array list for incoming device.
        IRDeviceConfig irDevice = irConfig.getNormalizedIrDevices().get(deviceId - 1);

        IRHardwareEnum irRemoteType = findIrRemoteType(irDevice.getType());

        return remoteFactory.getRemote(irRemoteType, irDevice.getHost(), keyset, irPort);
    }

}
