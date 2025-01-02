
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cats.configuration.IRConfiguration;
import com.cats.ir.redrathub.HubConnectionPool;
import com.cats.ir.IRCommunicator;
import com.cats.ir.IRCommunicatorManager;
import com.cats.ir.IRDevice;
import com.cats.ir.IRHardwareEnum;

import jakarta.annotation.PostConstruct;

/**
 * The local implementation for the communicator manager. This implementation assumes that there is a RedRathub running locally and,
 * all the redrat device are controlled by it.
 */
@Service
public class LocalIRCommunicatorManager implements IRCommunicatorManager {
    private static Logger logger = LoggerFactory.getLogger(LocalIRCommunicatorManager.class);
    public HubConnectionPool hubConnectionPool;

    @Autowired
    IRConfiguration irConfiguration;

    @PostConstruct
    public void init() {
        String ip = irConfiguration.getRedRatHubHost();
        hubConnectionPool = new HubConnectionPool(ip, Integer.parseInt(irConfiguration.getRedRatHubPort()));
    }

    @Override
    public IRCommunicator getCommunicator(IRDevice irDevice) {
        IRCommunicator communicator = null;
        IRHardwareEnum deviceType = irDevice.getDeviceType();
        switch (deviceType) {
            case IRNETBOXPRO3:
                // No separate logic for IRNETBOXPRO3
            case REDRAT3:
                logger.info("Going to instantiate IRNETBOXPRO3 communicator");

                communicator = hubConnectionPool.getConnection();
                break;
            case GC100:
            case GC100_12:
            case GC100_6:
            case ITACH:
                logger.info("GC communicator not implemented.");
                break;
            default:
                logger.info("Unknown communicator access request.");
                break;

        }
        return communicator;
    }

    @Override
    public void passivateCommunicator(IRCommunicator communicator) {
        hubConnectionPool.releaseConnection(communicator);
    }

}
