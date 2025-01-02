
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

import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.DelayCommand;
import com.cats.ir.commands.IrCommand;
import com.cats.ir.commands.PressKeyCommand;
import com.cats.ir.exception.IRCommunicatorNotInitializedException;
import com.cats.ir.exception.IRFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRCommunicator;
import com.cats.ir.IRCommunicatorManager;
import com.cats.ir.IRDevicePort;

/**
 * This class represents an abstract for all RedRat devices.
 */
public abstract class RedRatDevicePort extends IRDevicePort {

    private static final Logger logger = LoggerFactory.getLogger(RedRatDevicePort.class);

    /**
     * Represents a count value of 35 for press and hold.
     */
    private static final String REPEAT_COUNT_35 = "_repeat35";

    /**
     * Format for key that has a count value.
     */
    String repeatCountFormat = REPEAT_COUNT_35;

    private static final String PRESSKEY_EXPECTED_RESULT = "OK";

    public static final int WAIT_INTERVAL = 500;
    /**
     * Holds the reference of the communicator to be used for sending a a
     * request to the hub At this point no pooling of communicator is done. When
     * and if bring in pooling this instance variable should be removed and
     * getCommunicato() should be called on the communicatorManager each time a
     * command is to be send.
     */
    private IRCommunicator communicator;

    public RedRatDevicePort(RedRatDevice redratDevice, IRCommunicatorManager communicatorManager) {
        this.device = redratDevice;
        this.communicatorManager = communicatorManager;

    }

    @Override
    public synchronized Boolean sendCommand(CatsCommand catsCommand) throws IRFailureException,
            IRCommunicatorNotInitializedException {
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

    /**
     * Sends the command to the IR device
     *
     * @param commandString
     * @param expectedResult
     * @return
     * @throws IRCommunicatorNotInitializedException
     *             if the communicator has not been initialized
     * @throws IRFailureException
     */
    private Boolean sendCommand(String commandString, String expectedResult)
            throws IRCommunicatorNotInitializedException, IRFailureException {
        Boolean retVal = Boolean.TRUE;
        String response = null;

        getCommunicator();

        logger.debug("sendCommand()  on communicator{} ", communicator);
        response = sendTelnetCommand(commandString);
        logger.info("TELNET RESPONSE: {}", response);
        logger.debug("sendCommand()  response " + response);
        if (!expectedResult.equals(response)) {
            logger.info("error response " + response);
            if (response.contains("Failed to find signal ")) {
                logger.warn("Command " + commandString + " not valid for key set");
                throw new IllegalArgumentException("Command " + commandString + " not valid for key set");
            } else {
                throw new IRFailureException("RedRat did not return an expected Result. Command " + commandString
                        + " : Expected Result " + expectedResult + " : returned response " + response);
            }
        }

        return retVal;
    }

    @Override
    public boolean equals(Object irPort) {
        boolean isEqual = false;

        if (irPort != null && irPort instanceof RedRatDevicePort) {
            RedRatDevicePort rrPort = (RedRatDevicePort) irPort;
            if (rrPort.getIrDevice() != null && rrPort.getIrDevice().equals(this.getIrDevice())) {
                if (rrPort.portNumber == this.portNumber) {
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

    private String sendTelnetCommand(String command) {
        String retVal = "";
        int retries = 0;
        boolean tryRetry = false;
        do {
            try {
                logger.info("sendCommand " + command);
                long start = System.currentTimeMillis();
                retVal = communicator.sendCommand(command);
                long end = System.currentTimeMillis();
                if (end - start > 1500) {
                    logger.warn("sendCommand " + command + " response " + retVal + " time taken by hub "
                            + (end - start) + "ms");
                } else {
                    logger.trace("sendCommand " + command + " response " + retVal + " time taken by hub "
                            + (end - start) + "ms");
                }
                tryRetry = false;
            } catch (IOException e) {
                logger.warn("connectTelnet failed " + e.getMessage());
                tryRetry = true;
                retries++;
                if (retries < 3) {
                    try {
                        communicator.closeConnection();
                        communicator.connect(false);
                    } catch (IOException e2) {
                        logger.warn("Could not reconnect. The hub may have crashed. " + e2.getMessage());
                    }
                }
                try {
                    Thread.sleep(WAIT_INTERVAL);
                } catch (InterruptedException e1) {
                    logger.warn("connectTelnet wait interrupted " + e1.getMessage());
                }
            }
        } while (tryRetry && retries < 3);

        //release connection back to pool.
        releaseConnection();

        return retVal;
    }

    @Override
    public int hashCode() {
        // all ports of this device can be grouped.
        int hashCode = 0;
        if (getIrDevice() != null) {
            hashCode = getIrDevice().getId().hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "device :" + getIrDevice() + " port " + getPortNumber();
    }

    private void getCommunicator() throws IRCommunicatorNotInitializedException {
        try {
            this.communicator = communicatorManager.getCommunicator(device);
            this.communicator.connect(false);
        } catch (Exception e) {
            logger.error("Communicator init for to the dvice:{} port:{} failed", device.getDeviceIp(), getPortNumber());
            releaseConnection();
            e.printStackTrace();

        }
        if (communicator == null) {
            throw new IRCommunicatorNotInitializedException("Communicator for: device" + device.getDeviceIp());
        }

    }

    public void releaseConnection() {
        logger.info("Release telnetConnection to pool");
        communicatorManager.passivateCommunicator(communicator);
        communicator = null;// resetting to null as this is an unusable
        // communicator.
    }

    /**
     * The implementation of this method varies based on type of Redrat device.
     * This method would return the appropriate command to be send to the redrat
     * hub.
     *
     * @param command
     * @return
     */
    protected abstract String getDeviceUnderstandablePressKeyCommand(CatsCommand command);

}
