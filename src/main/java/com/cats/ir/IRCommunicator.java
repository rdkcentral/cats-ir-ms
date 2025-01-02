
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

import java.io.IOException;
import java.net.SocketException;

import com.cats.ir.exception.IRFailureException;

/**
 * Interface defining the actual communicator used by the IRDevicePorts to
 * communicate with the RedRatHub or the GC100.
 */
public interface IRCommunicator {
    public boolean isConnected();

    public boolean connect(boolean isEnterRequired) throws IOException, SocketException;

    /**
     * The following method is used explicitly by the RedRatCommunicator
     *
     * @param command
     * @param prompt
     * @return
     * @throws IRFailureException
     */
    public String sendCommand(String command) throws IOException;

    public String sendCommand(String command, String prompt) throws IOException;

    /**
     * The following method will be implemented by the GlobalCacheCommunicator.
     *
     * @param command
     * @param port
     * @return
     * @throws IRFailureException
     */
    public String sendCommand(String command, int port) throws IOException;

    /**
     * The following method will be implemented by the GlobalCacheCommunicator
     * in case of sending command with repeat count( press and hold).
     *
     * @param command
     * @param port
     * @param count
     * @param offset
     * @return
     * @throws IRFailureException
     */
    public String sendCommand(String command, int port, int count, int offset) throws IOException;

    /**
     * Return the host address at which this communicator is connected to
     *
     * @return
     */
    public String getHost();

    /**
     * Return the host address at which this communicator is connected to
     *
     * @return
     */
    public Integer getPort();

    public void closeConnection();
}
