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

import com.cats.ir.IRDevicePort;
import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.DelayCommand;
import com.cats.ir.commands.IrCommand;
import com.cats.ir.commands.PressKeyCommand;
import com.cats.ir.exception.IRCommunicatorNotInitializedException;
import com.cats.ir.exception.IRFailureException;
import com.cats.configuration.CustomApplicationContext;
import com.cats.utils.GCDispatcherUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GCDevicePort.
 * This class is to manage and interact with the Global Cache device port.
 * Provides methods to manage and interact with the device, including retrieving
 * device ports and handling device-specific configurations.
 */
@Slf4j
public abstract class GCDevicePort extends IRDevicePort {

    private static final Logger logger = LoggerFactory.getLogger(GCDevicePort.class);

    private static final String PRESSKEY_EXPECTED_RESULT = "success";

    public GCDevicePort(GCDevice gcDevice) {
        this.device = gcDevice;
    }

    @Override
    public Boolean sendCommand(CatsCommand catsCommand) throws IRFailureException, IRCommunicatorNotInitializedException {
        Boolean retVal = true;
        String commandString = null;
        String expectedResult = null;

        logger.debug("sendCommand() CatsCommand " + catsCommand);


        if (catsCommand != null) {
            while (catsCommand.hasNext()) {
                CatsCommand command = catsCommand.next();
                if (command instanceof DelayCommand) {
                    logger.debug("Its a DelayCommand() " + command);
                    try {
                        Thread.sleep(((DelayCommand) command).getDelay());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (command instanceof IrCommand) {
                    logger.debug("sendCommand() catsCommand.next() " + command);
                    commandString = getDeviceUnderstandablePressKeyCommand(command);
                    expectedResult = getExpectedResult(command);
                    logger.debug("commandString " + commandString + " expectedResult  " + expectedResult);
                    if (commandString != null) {
                        retVal = sendCommand(commandString, expectedResult);
                    } else {
                        throw new IRFailureException("IrNetBoxPro does not know how to handle this command :"
                                + commandString);

                    }
                } else {
                    logger.debug("Got an CatsCommand which has no implementation : {} ", command);
                }

            }
        } else {
            throw new IRFailureException("Command is null");
        }

        return retVal;
    }

    @Override
    public boolean equals(Object irPort) {
        boolean isEqual = false;

        if (irPort != null) {
            GCDevicePort gcPort = (GCDevicePort) irPort;
            if (gcPort.getIrDevice() != null && gcPort.getIrDevice().equals(this.getIrDevice())) {
                if (gcPort.portNumber == this.portNumber) {
                    isEqual = true;
                }
            }
        }
        logger.debug("isEqual: irPort " + irPort + " : this : " + this + " equals? " + isEqual);
        return isEqual;
    }

    private String getExpectedResult(CatsCommand command) {
        String expectedResult = null;

        if (command instanceof PressKeyCommand) {
            expectedResult = PRESSKEY_EXPECTED_RESULT;
        }

        return expectedResult;
    }


    /**
     * Gets the device understandable press key command.
     *
     * @param command the command
     * @return the device understandable press key command
     */
    protected abstract String getDeviceUnderstandablePressKeyCommand(CatsCommand command);

    private Boolean sendCommand(String commandString, String expectedResult) throws IRFailureException {
        boolean retVal;
        String gcdispatcherApiBase = ((GCDevice) device).getGetGcDispatcherApiBase();
        String urlEndpoint = String.format(gcdispatcherApiBase + "/press_key");
        GCDispatcherUtil httpUtil = CustomApplicationContext.getBean(GCDispatcherUtil.class);
        return httpUtil.sendCommand(commandString,expectedResult, urlEndpoint);
    }
}