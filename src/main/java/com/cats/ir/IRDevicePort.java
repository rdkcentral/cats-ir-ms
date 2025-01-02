
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

import com.cats.ir.commands.CatsCommand;
import com.cats.ir.exception.IRCommunicatorNotInitializedException;
import com.cats.ir.exception.IRFailureException;

/**
 * Represents a port on the {@link IRDevice}
  */
public abstract class IRDevicePort {

    //portNumber is the number of the port the device is on
    protected int portNumber;

    //device is the device on the port
    protected IRDevice device;

    protected IRCommunicatorManager communicatorManager;

    /**
     * Get the number of this port
     *
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Get the IrDevice that this port belongs to.
     *
     */
    public IRDevice getIrDevice() {
        return device;
    }

    /**
     * Send a CATSCommand to the device at this port.
     *
     */
    public abstract Boolean sendCommand(CatsCommand command) throws IRFailureException,
            IRCommunicatorNotInitializedException;
}
