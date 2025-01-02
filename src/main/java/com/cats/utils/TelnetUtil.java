
package com.cats.utils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to handle telnet connections.
 */
public class TelnetUtil {
    public static final String ERROR_STRING = "Error Occurred : ";

    public static final int WAIT_INTERVAL = 10 * 60;                                    // 10sec

    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class);

    /**
     * Connect to a {@link TelnetConnection}.
     *
     * @param telnetConnection
     * @return true if connected successfully.
     */
    public static synchronized boolean connectTelnet(TelnetConnection telnetConnection) {
        boolean retVal = false;
        int retries = 0;
        boolean tryRetry;

        if (telnetConnection != null) {
            do {
                logger.debug("ConnectTelnet retries " + retries);
                try {
                    retVal = telnetConnection.connect(false);
                    logger.debug("connectTelnet status " + retVal);
                    tryRetry = retVal;
                    break;
                } catch (IOException e) {
                    logger.warn("connectTelnet failed " + e.getMessage());
                    tryRetry = true;
                    retries++;
                }

                try {
                    Thread.sleep(WAIT_INTERVAL);
                } catch (InterruptedException e) {
                    logger.warn("connectTelnet wait interrupted " + e.getMessage());
                }
            } while (tryRetry && retries < 3);
        }

        return retVal;
    }

    /**
     * Send a command to a telnet Connection. It must be ensured that the
     * {@link TelnetConnection} is already connected.
     *
     * @param telnetConnection
     * @param command
     * @return
     */
    public static String sendTelnetCommand(TelnetConnection telnetConnection, String command, String promptString) {
        synchronized (new Object()) {
            String retVal = ERROR_STRING;
            if (telnetConnection == null || command == null) {
                retVal += "TelnetConnection or command String should not be null. TelnetConnection : "
                        + telnetConnection + " Command : " + command;
            } else {
                int retries = 0;
                boolean tryRetry = false;
                do {
                    try {
                        tryRetry = false;
                        retVal = telnetConnection.sendCommand(command, promptString);
                        if (retVal == null) {
                            logger.warn("sendTelnetCommand returned null ");
                            retVal = ERROR_STRING
                                    + "sendCommand returned null. Maybe telnet connection is not in connected state.";
                        }
                    } catch (IOException e) {
                        logger.warn("sendTelnetCommand failed " + e.getMessage());
                        telnetConnection.closeConnection();
                        connectTelnet(telnetConnection);
                        retVal += e.getMessage();
                        retries++;
                        tryRetry = true;
                    }
                } while (tryRetry && retries < 2);
            }

            return retVal;
        }
    }

}
