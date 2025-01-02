
package com.cats.ir.exception;

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

/**
 * Exception thrown by the {@link IRDevicePort} if a sendCommand is invoked
 * without initializing a communicator.
 */
public class IRCommunicatorNotInitializedException extends Exception {

    String deviceIP;

    private static final long serialVersionUID = 1L;

    public IRCommunicatorNotInitializedException(String message, String deviceIp) {
        super(message + ", deviceIP:" + deviceIp);
        this.deviceIP = deviceIp;
    }

    public IRCommunicatorNotInitializedException(String deviceIp) {
        super(" IR Communicator is not initialized for deviceIP:[ " + deviceIp + " ]");
    }
}
