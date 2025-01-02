package com.cats.ir.gc.gc100;

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
import com.cats.ir.gc.GCCommands;
import com.cats.ir.redrat.RedRatCommands;
import com.cats.ir.IRCommunicator;
import com.cats.ir.gc.GCDevicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GC100Port.
 * This class is to manage and interact with the Global Cache device port.
 * Provides methods to manage and interact with the device, including retrieving
 * device ports and handling device-specific configurations.
 */
public class GC100Port extends GCDevicePort {

    private static final Logger logger = LoggerFactory.getLogger(GC100Port.class);


    public GC100Port(int portNumber, GC100 irDevice, IRCommunicator manager) {
        super(irDevice);
        this.portNumber = portNumber;
    }


    protected String getDeviceUnderstandablePressKeyCommand(CatsCommand command) {
        String commandString = null;

        if (command instanceof PressKeyCommand) {
            PressKeyCommand pressKeyCommand = (PressKeyCommand) command;
            commandString = GCCommands.SEND_COMMAND.replace(GCCommands.MODULE_ARGUMENT, device.getDeviceModule());

            String key = pressKeyCommand.getRemoteCommand().toString();
            if (command instanceof PressKeyAndHoldCommand) {
                if (((PressKeyAndHoldCommand) command).getMode() == PressKeyAndHoldCommand.REPEAT_MODE) {
                    logger.debug("PressKeyAndHoldCommand.REPEAT_MODE ");
                    commandString = commandString.concat(RedRatCommands.IRNETBOX_REPEAT_COMMAND_APPEND).replace(RedRatCommands.REPEAT_ARGUMENT,
                            ((PressKeyAndHoldCommand) command).getCount().toString());
                } else if (((PressKeyAndHoldCommand) command).getMode() == PressKeyAndHoldCommand.DURATION_MODE) {
                    logger.debug("PressKeyAndHoldCommand.DURATION_MODE");
                    commandString = commandString.concat(RedRatCommands.IRNETBOX_DURATION_COMMAND_APPEND).replace(
                            RedRatCommands.DURATION_ARGUMENT, "" + (((PressKeyAndHoldCommand) command).getDuration() * 1000));
                    logger.info("PressKeyAndHoldCommand.DURATION_MODE commandString " + commandString);
                }
            }
            commandString = commandString.replace(RedRatCommands.KEY_ARGUMENT, key);
            logger.debug("Sending command[{}]  to device[{}]", commandString, device);
        }
        return commandString;
    }
}
