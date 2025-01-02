
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


import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.PressKeyAndHoldCommand;
import com.cats.ir.commands.PressKeyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRCommunicatorManager;

/**
 * Represents a port of the RedRat3 USB device. There will only be one port
 * associated with this device
 */
public class RedRat3Port extends RedRatDevicePort {

    private Logger logger = LoggerFactory.getLogger(RedRatDevicePort.class);

    public RedRat3Port(RedRat3 irDevice, IRCommunicatorManager manager) {
        super(irDevice, manager);
        portNumber = RedRat3.RED_RAT3_PORT;
        logger.info("Set the name of the RedRat3 device to :{}", irDevice.getDeviceIp());
    }

    protected String getDeviceUnderstandablePressKeyCommand(CatsCommand command) {
        String commandString = null;
        if (command instanceof PressKeyCommand) {
            PressKeyCommand pressKeyCommand = (PressKeyCommand) command;
            //changing the deviceIP to deviceName so that multiple devices can be accessed at once
            commandString = RedRatCommands.REDRAT3_IR_COMMAND.replace(RedRatCommands.NAME_ARGUMENT, ((RedRat3) device).getDeviceName())
                    .replace(RedRatCommands.KEYSET_ARGUMENT, pressKeyCommand.getIrKeySet());

            String key = pressKeyCommand.getRemoteCommand().toString();
            if (command instanceof PressKeyAndHoldCommand) {
                key = key.concat(repeatCountFormat);
            }
            commandString = commandString.replace(RedRatCommands.KEY_ARGUMENT, key);
        }
        return commandString;
    }

}
