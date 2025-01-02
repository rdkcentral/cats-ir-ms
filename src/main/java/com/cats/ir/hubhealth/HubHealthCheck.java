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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cats.ir.IRCommunicator;
import com.cats.ir.IRHardwareEnum;
import com.cats.ir.Remote;
import com.cats.ir.RemoteFactory;
import com.cats.ir.*;
import com.cats.ir.manager.LocalIRCommunicatorManager;
import com.cats.ir.redrathub.HubConnectionPool;
import com.cats.service.DependencyHealthCheck;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cats.ir.redrat.RedRatCommands;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
public class HubHealthCheck {

    Logger logger = LoggerFactory.getLogger(HubHealthCheck.class);

    HubConnectionPool hcp;

    static HashMap<String, String> redRatCommands;
    static HashMap<String, String> endChars;
    AtomicLong connectionCloseCount;
    @Autowired
    LocalIRCommunicatorManager localIRCommunicatorManager;

    @Autowired
    DependencyHealthCheck dependencyHealthCheck;
    @Getter
    HubHealthBean healthBean;

    static {
        redRatCommands = new HashMap<String, String>();
        redRatCommands.put("listRedRats", RedRatCommands.LIST_REDRATS);
        redRatCommands.put("hubVersion", "hubQuery=\"hub version\"");
        redRatCommands.put("keysets", RedRatCommands.LIST_DATASETS);
        redRatCommands.put("firmwareVersion", "hardwareQuery=\"firmware version\" ip=\"");
        redRatCommands.put("hardwareType", "hardwareQuery=\"hardware type\" ip=\"");

        endChars = new HashMap<>();
        endChars.put("listRedRats", "}");
        endChars.put("keysets", "}");
        endChars.put("hubVersion", ")");
        //Define a shortcut pattern to only read single line.
        endChars.put("firmwareVersion", "LINE");
        endChars.put("hardwareType", "LINE");
    }


    @PostConstruct
    public void init() {
        if (dependencyHealthCheck.checkHubHealth()) {
            this.hcp = localIRCommunicatorManager.hubConnectionPool;
        }
        this.connectionCloseCount = new AtomicLong(0);
        process();
    }

    public void process() {
        if (this.hcp == null) {
            healthBean = null;
            return;
        }
        healthBean = new HubHealthBean();
        try {
            String hubVersionResult = sendCommand("hubVersion");
            Map<String, String> hubVersion = parseHubVersion(hubVersionResult);
            healthBean.setHubVersion(hubVersion);

            // Retrieve keys
            String keysetResult = sendCommand("keysets");
            List<String> keysets = parseKeysets(keysetResult);
            healthBean.setKeysets(keysets);

            // Retrieve red rat devices.
            String listRedRatDevicesResult = sendCommand("listRedRats");
            List<RedRatDeviceBean> devices = parseRedRatDeviceList(listRedRatDevicesResult);

            // For each device in devices...
            for (RedRatDeviceBean device : devices) {
                String deviceIp = device.getIp();
                //Only try to query connected irNetBox devices.
                if (device.getStatus().equalsIgnoreCase("connected")) {
                    String firmwareVersion = sendCommandWithIp("firmwareVersion", deviceIp);
                    device.setFirmwareVersion(firmwareVersion);
                    String hardwareType = sendCommandWithIp("hardwareType", deviceIp);
                    device.setHardwareType(hardwareType);
                }
            }
            healthBean.setDevices(devices);
            //hubUp.set(1);
            healthBean.setHubUp(true);
        } catch (Exception e) {
            //hubUp.set(0);
            healthBean.setHubUp(false);
        }
    }

    /**
     * Sends specific commands to RedRatHub to retrieve health status.
     *
     * @param request The health info to be retrieved
     * @return the return string from RedRatHub, empty string if request is invalid.
     */
    public String sendCommand(String request) {
        if (!redRatCommands.keySet().contains(request)) {
            logger.warn("HubHealthCheck.sendCommand(): Invalid request");
            return "";
        }

        logger.info("Attempting sendCommand({})", request);
        String command = redRatCommands.get(request);
        IRCommunicator comm = null;
        String result = "";
        try {
            comm = hcp.getConnection();
            if (comm == null) {
                logger.warn("IRCommunicator is NULL");
                throw new IOException("No IRCommunicator Available");
            }
            if (comm.isConnected()) {
                logger.info("HubHealthCheck.sendCommand()");
                if (endChars.keySet().contains(request)) {
                    result = comm.sendCommand(command, endChars.get(request));
                } else {
                    result = comm.sendCommand(command); // Use default prompt/end character.
                }
            } else {
                logger.warn("sendCommand(): IRCommunicator NOT Connected");
            }
        } catch (Exception e) {
            logger.error("HubHealthCheck.sendCommand(): Could not connect to hub[{}:{}] Message[{}]", hcp.getConnection().getHost(), hcp.getConnection().getPort(), e.getLocalizedMessage());
        }
        hcp.releaseConnection(comm);
        logger.info("Complete sendCommand({}) with Response[{}]", request, result);
        return result;
    }

    /**
     * Handles commands to RedRatHub that require IP address to retrieve health status.
     *
     * @param request The information to retrieve from RedRatHub.
     * @return The response from RedRatHub.
     */
    public String sendCommandWithIp(String request, String ip) {
        if (!redRatCommands.keySet().contains(request)) {
            logger.warn("HubHealthCheck.sendCommandWithIp(): Invalid request");
            return "";
        }
        logger.info("HubHealthCheck.sendCommandWithIp({}, {})", request, ip);
        String command = redRatCommands.get(request);
        IRCommunicator comm = null;
        String result = "";
        try {
            comm = hcp.getConnection();
            if (comm == null) {
                logger.warn("IRCommunicator is NULL");
                throw new IOException("No IRCommunicator Available");
            }
            if (comm.isConnected()) {
                logger.info("HubHealthCheck.sendCommand()");
                if (endChars.keySet().contains(request)) {
                    result = comm.sendCommand(command + ip + "\"", endChars.get(request));
                } else {
                    result = comm.sendCommand(command + ip + "\"");
                }
            } else {
                logger.warn("sendCommandWithIp(): IRCommunictor NOT Connected");
            }
        } catch (Exception e) {
            logger.error("HubHealthCheck.sendCommandWithIp(): Exception while proessing command", e);
        }
        hcp.releaseConnection(comm);
        logger.info("HubHealthCheck.sendCommandWithIp({},{}) Response[{}]", request, ip, result);
        return result;
    }

    public void stats() {
        logger.info("HubConnectionPool Active[{}]", hcp.getActive());
        logger.info("Connection Close Count [{}]", this.connectionCloseCount.get());
    }

    public void restart() {
        hcp.init();
    }

    public AtomicLong getConnectionCloseCount() {
        return connectionCloseCount;
    }

    /**
     * Parses the string of individual keysets provided by RedRatHub into a list.
     *
     * @param rawKeysets The raw string from RedRatHub request.
     * @return A list of keysets this hub accepts.
     */
    public static ArrayList<String> parseKeysets(String rawKeysets) {
        String[] keysets = rawKeysets.split("\\n");

        // Handles case where rawKeysets was {} , no keysets.
        if (keysets.length == 1) {
            return new ArrayList<String>();
        }

        for (String keyset : keysets) {
            keyset = keyset.trim();
        }
        ArrayList<String> keysetList = new ArrayList<String>(Arrays.asList(keysets));
        keysetList.remove(0); // Removes the start of list "{"
        keysetList.remove(keysetList.size() - 1);  // Removes the ending of the list with "}"
        return keysetList;
    }

    /**
     * Parses a string of hub components and their versions into a list.
     *
     * @param rawHubVersion The raw string of hub versions from RedRatHub request.
     * @return A map of hub components (key) and their versions (value).
     */
    public static Map<String, String> parseHubVersion(String rawHubVersion) {
        Map<String, String> hubVersion = new HashMap<String, String>();
        // Handles error when there are no hub version info sent from RRH.
        if (rawHubVersion.equals("")) {
            return new HashMap<String, String>();
        }
        String[] hubComponents = rawHubVersion.split(", ");
        for (String component : hubComponents) {
            String[] componentAndVersion = component.split(" ");
            String componentName = componentAndVersion[0].trim();
            String version = componentAndVersion[1].trim();
            version = version.substring(1, version.length() - 1); // Remove parentheses around version number.
            hubVersion.put(componentName, version);
        }
        return hubVersion;
    }

    /**
     * Parses string to create a list of RedRatDeviceBean objects.
     *
     * @param rawRedRatDeviceList The raw string of details (type, mac address, ip address, and status) of RedRat devices.
     * @return A list of RedRatDevices associated with this hub.
     */
    public static List<RedRatDeviceBean> parseRedRatDeviceList(String rawRedRatDeviceList) {
        List<RedRatDeviceBean> devices = new ArrayList<>();

        String[] splitDevices = rawRedRatDeviceList.split("\\n"); // Holds the strings of details about individual devices
        // For each device string, split into its details.
        for (int i = 0; i < splitDevices.length; i++) {
            if (splitDevices[i].startsWith("{") || splitDevices[i].startsWith("}")) {
                //Ignore JSON like syntax.
                continue;
            }
            String patternRegex = "(\\[.+\\]).+(\\(.+\\))\\sat\\s(.+)";
            Pattern pattern = Pattern.compile(patternRegex);
            Matcher matcher = pattern.matcher(splitDevices[i]);
            if (matcher.find()) {
                String type = matcher.group(1);
                type = type.substring(1, type.length() - 1); // Removes brackets around type.
                String mac = matcher.group(2);
                mac = mac.substring(1, mac.length() - 1); // Remove parentheses around mac address.
                mac = mac.replace("-", ":");
                String ip = matcher.group(3);
                String status = "";
                if (ip.matches("(.+)\\s(\\(.+\\))")) { // The device string contains a status.
                    String[] ipAndStatus = ip.split(" ");
                    ip = ipAndStatus[0];
                    status = ipAndStatus[1].substring(1, ipAndStatus[1].length() - 1); // Removes paretheses around status
                }
                RedRatDeviceBean deviceBean = new RedRatDeviceBean(type, mac, ip, status);
                devices.add(deviceBean);
            }
        }
        Collections.sort(devices); // sorts list by ascending device ip address.

        return devices;
    }

    public boolean isHubHealthOutdated(List<RedRatDeviceBean> hubDevices, RemoteFactory factory) {
        boolean isHealthOutdated = false;

        try {
            for (RedRatDeviceBean deviceBean : hubDevices) {
                if (!deviceBean.getStatus().equalsIgnoreCase("connected")) {
                    //Sending a command to the 1st port of each Device to reset connection.
                    Remote remote = factory.getRemote(IRHardwareEnum.IRNETBOXPRO3, deviceBean.getIp(), "PC_REMOTE", 1);
                    isHealthOutdated = remote.pressKey("VOLUP");
                }
            }
        } catch (Exception exception) {
            log.warn(exception.getMessage());
        }

        return isHealthOutdated;
    }

}
