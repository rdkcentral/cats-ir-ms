
package com.cats.ir.redrathub;

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
import java.util.concurrent.atomic.AtomicLong;

import com.cats.ir.IRCommunicator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.utils.TelnetConnection;

/**
 * The RedRat Communicator used to send commands to the RedRat Hub
 */
@Slf4j
public class RedRatHubCommunicator extends TelnetConnection implements IRCommunicator {
    protected Long instanceId = -1L;
    protected Long transactionId;
    protected AtomicLong requests;

    private static final Logger logger = LoggerFactory.getLogger(RedRatHubCommunicator.class);

    public RedRatHubCommunicator(String redratHubHost, int redratHubPort, String redratPromptString, Long instanceId) {
        super(redratHubHost, redratHubPort, redratPromptString);
        this.requests = new AtomicLong(0);
        this.instanceId = instanceId;
    }

    /**
     * Wrapper to TelnetConnection to ensure that connection is released back to
     * pool after use.
     */
    @Override
    public String sendCommand(String command, String prompt) throws IOException, SocketException {
        logger.info("sendCommand[{},{}] Count[{}] Command: [{}]", instanceId, transactionId, requests.incrementAndGet(), command);
        String retVal = super.sendCommand(command, prompt);

        /**
         * Given wide range of responses
         */
        if (logger.isDebugEnabled()) {
            logger.debug("sendCommand[{},{}] Response: {}", instanceId, transactionId, retVal);
        }
        return retVal;
    }

    @Override
    public String sendCommand(String command, int port) throws IOException {
        throw new UnsupportedOperationException(
                "send Command with command and port not support in RedRatHubConnection");
    }

    @Override
    public String sendCommand(String command, int port, int count, int offset) throws IOException, SocketException {
        throw new UnsupportedOperationException(
                "send Command with command and port not support in RedRatHubConnection");
    }

    @Override
    public boolean isConnected() {
        return super.isConnected();
    }


    @Override
    public String sendCommand(String command) throws IOException, SocketException {
        requests.incrementAndGet();
        logger.info("sendCommand[{},{}] Command: [{}]", instanceId, transactionId, command);
        String retVal = super.sendCommand(command);
        return retVal;
    }

    @Override
    public String getHost() {
        return super.getHost();
    }

    @Override
    public Integer getPort() {
        return super.getPort();
    }

    public Long getRequests() {
        return requests.get();
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}
